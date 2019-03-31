// This is the entry point for AST-based clone detector

package fett

import org.rogach.scallop._;

// import notjs.syntax._
import notjs.translator._
import java.io._
import notjs.translator.jsast._
import fett.sexp._
import fett.scoring._
import fett.reporting.HTMLReporter
import scala.util.matching.Regex
import scala.concurrent._
import scala.concurrent.duration.Duration
import java.util.concurrent.{Executors, ExecutorService}
import java.util.Arrays.binarySearch
import scala.collection.mutable.{MutableList => MList, Set => MSet, Map => MMap}
import scala.util._

import scala.language.reflectiveCalls

import util.logger
import util.profiling.profile
import fett.util._
import fett.util.Implicits._
import fett.util.trees._
import fett.util.utils._
import fett.util.utils.Implicits._
import fett.parsing._

//-----------------------------------------------------
// Command-line configuration
case class CLIConf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val program = "fett"
  version(s"$program ${util.info.version}")
  banner("""
           |    ______     __  __ 
           |   / ____/__  / /_/ /_
           |  / /_  / _ \/ __/ __/
           | / __/ /  __/ /_/ /_  
           |/_/    \___/\__/\__/
           |
           | Usage: fett [options] file1.js file2.js ...
           |
           |""".stripMargin)

  val scorers = Map(
    "dummy" → ((_:Map[String, String]) ⇒ DummyScoring),
    "smith-waterman" → SmithWaterman.fromOptions _,
    "tree-edit-distance" → TreeEditDistance.fromOptions _,
    "zhang-shasha" → ZhangShasha.fromOptions _,
    "needleman-wunsch" → ((_:Map[String, String]) ⇒ NeedlemanWunsch()),
    "greedy-string-tiling" → ((_:Map[String, String]) ⇒ GreedyStringTiling()),
    "winnowing" → Winnowing.fromOptions _,
    "winnowing2" → ((_:Map[String, String]) ⇒ Winnowing2()),
    "mining" → ((_:Map[String, String]) ⇒ Mining),
    "id-similarity" → ((_:Map[String, String]) ⇒ IdSimilarity),
    "combined-lawton" → CombinedLawton.fromOptions _,
    "combined-max" → CombinedMax.fromOptions _,
    "combined-min" → CombinedMin.fromOptions _,
    "tree-align" → TreeAlign.fromOptions _,
    "zhang13-smith-waterman" → Zhang13SmithWaterman.fromOptions _
  )


  val dirMode = opt[Boolean]("directory-mode", descr="Process input files as directories and search for files in them.")
  val recursive = opt[Boolean]("recursive", descr="Process input directories recursively to find files. Implies ${dirMode.nm}.")

  val report = opt[String]("report", descr="output an html report file containing all the pairs and the matches.")

  val debug = opt[Boolean]("debug")

  val jobs = opt[Int]("jobs", descr="number of workers to use for parallel processing", default=Some(1))

  val exclude = opt[String]("exclude", descr="Give a regex pattern to exclude files.")
  val include = opt[String]("include", descr=s"Give a regex pattern to include files. If both include and exclude options are given, $program will check if files are in L(include) \\ L(exclude).")
  // options
  val scoring = opt[Map[String, String] ⇒ Scoring]("scoring",
    default=Some(SmithWaterman.fromOptions),
    descr="Scoring algorithm, defaults to Smith-Waterman. Available scoring algorithms: " + scorers.keys.mkString(", ") + "."){
    singleArgConverter { scorer ⇒
      scorers.getOrElse(scorer.toLowerCase,
        sys.error(s"Unknown scoring algorithm: $scorer"))
    }
  }

  val filterFile = opt[File]("pair-filter", descr="Filter file to filter pairs of functions to compare. The format is a CSV file consisting of pairs of functions.")
  validateFileExists(filterFile)

  val granularity = opt[String]("granularity", descr="{function, file}", default=Some("file"))
  val writeToCSV = opt[String]("csv-file", descr="File name to store pairwise results in CSV format")

  val options = props[String]('X', descr="Options for scoring algorithm")

  val topNHTML = opt[String]("html-top-N", descr="File name to store html top N results")
  val topN = opt[Int]("N", descr="# of results per language to put in html top N file", default=Some(5))

  val allpairs = new Subcommand("allpairs") {
    // files
    val files = trailArg[List[String]]()

    descr("Compute matches between all files in all directories.")
  }
  addSubcommand(allpairs)

  val compare = new Subcommand("compare") {
    // two directories to compare
    val a = trailArg[String]()
    val b = trailArg[String]()

    descr("Cross-match. Compute matches between files in given two input directories.")
  }
  addSubcommand(compare)

  val cutoff = new Subcommand("cutoff") {
    // two directories to compare
    val cutoff = trailArg[Double]()
    val a = trailArg[String]()
    val b = trailArg[String]()

    descr("Cross-match with cutoff. Compute matches between files in given two input directories with cutoff-based optimization.")
  }
  addSubcommand(cutoff)

  verify()
}

//-----------------------------------------------------
// Clone detector entry point

object CloneDetection {

  def filenameFilter(conf: CLIConf): FilenameFilter = {
    @inline def matches(P: Regex): String ⇒ Boolean = {
      _ match {
        case P() ⇒ true
        case _ ⇒ false
      }
    }

    val acceptor: (File, String) ⇒ Boolean = (conf.include.toOption, conf.exclude.toOption) match {
      case (Some(include), None) ⇒ {
        val regex = include.r

        (dir, fName) ⇒ matches(regex)(fName)
      }
      case (None, Some(exclude)) ⇒ {
        val regex = exclude.r

        (dir, fName) ⇒ ! matches(regex)(fName)
      }
      case (Some(include), Some(exclude)) ⇒ {
        val includeMatcher = matches(include.r)
        val excludeMatcher = matches(exclude.r)

        (dir, fName) ⇒ {
          includeMatcher(fName) && (! excludeMatcher(fName))
        }
      }
      case (None, None) ⇒ (dir, fName) ⇒ true
    }

    new FilenameFilter {
      def accept(dir: File, fName: String) = acceptor(dir, fName)
    }
  }

  def checkFile(f: File): Unit = {
    if (! f.exists) {
      sys.error(s"Input file ${f.getPath} does not exist.")
    }
    if (! f.isFile) {
      sys.error(s"Input file ${f.getPath} is not a regular file.")
    }
  }

  def checkDir(d: File): Unit = {
    if (! d.exists) {
      sys.error(s"Input directory ${d.getPath} does not exist.")
    }
    if (! d.isDirectory) {
      sys.error(s"Input directory ${d.getPath} is not actually a directory.")
    }
  }

  /** Extract files from configuration considering directory mode etc.
    */
  def getFiles(conf: CLIConf, inFiles: List[String], filter: Filter): Seq[File] = {
    val dirMode = conf.dirMode() || conf.subcommand == Some(conf.compare) || conf.recursive()
    val recursive = conf.recursive()
    val files = inFiles.map((fName) ⇒ new File(fName))
    val fNameFilter = filenameFilter(conf) & filter.fileFilter
    val fs = if (dirMode) {
      if (recursive) {
        var worklist: List[File] = files
        worklist foreach checkDir
        var fileList = List.empty[File]
        while (worklist.nonEmpty) {
          val dir = worklist.head
          worklist = worklist.tail
          for (f ← dir.listFiles) {
            if (f.isDirectory) {
              worklist +:= f
            } else if (f.isFile && fNameFilter.accept(dir, f.getName)) {
              fileList +:= f
            }
          }
        }
        fileList
      } else {
        files flatMap { dir ⇒
          checkDir(dir)
          dir.listFiles(fNameFilter).filter(_.isFile)
        }
      }
    } else {
      files foreach checkFile
      files
    }
    println(s"Found ${fs.size} files")
    fs
  }

  def allPairs(conf: CLIConf, inFiles: List[String], filter: Filter) = {
    val files = getFiles(conf, inFiles, filter)
    logger.debug(s"Files: $files")

    val scorer = conf.scoring()
    logger.debug("Parsing ASTs and creating ParseTreeNodes")

    val granularity = conf.granularity()

    val parseTreeNodes = profile("Reading ASTs") {
      // scala.util.Random.shuffle(files).take(20).map((f:File) ⇒ f → readAST(f)).toList
      files.flatMap((f:File) ⇒ readAST(Some(conf), f, granularity).map(sexp => f -> sexp) ).toList
    }
    println(s"Found ${parseTreeNodes.size} granules to process")

    // collect all the node names, print them out, and exit
    // var nodeNames = Set[Symbol]()
    // for ((_, sexp) <- parseTreeNodes) {
    //   nodeNames = nodeNames ++ sexp.preOrderFunctions.concat
    //   // nodeNames = nodeNames ++ sexp.preOrder
    // }
    // println(nodeNames)
    // System.exit(0)

    val total = parseTreeNodes.length * (parseTreeNodes.length - 1) / 2
    (total, parseTreeNodes.combinations(2))
  }

  def compareDirs(conf: CLIConf, dirA: String, dirB: String, filter: Filter): Stream[List[(File, ParseTreeNode)]] = {
    val filesA = getFiles(conf, List(dirA), filter)
    logger.debug(s"Files in A: $filesA")
    val filesB = getFiles(conf, List(dirB), filter)
    logger.debug(s"Files in B: $filesB")
    logger.debug("Parsing ASTs and creating ParseTreeNodes")

    val granularity = conf.granularity()

    val (parseTreeNodesA, parseTreeNodesB) = profile("Reading ASTs") {
      val readASTs = (fs: Seq[File]) ⇒ fs.flatMap(f ⇒ readAST(Some(conf), f, granularity).map(sexp => f -> sexp) )
      (readASTs(filesA).toStream, readASTs(filesB).toStream)
    }

    println(s"Found ${parseTreeNodesA.size} granules to process from A")
    println(s"Found ${parseTreeNodesB.size} granules to process from B")

    for {
      a ← parseTreeNodesA
      b ← parseTreeNodesB
    } yield List(a, b)
  }

  def cutoffPairs(conf: CLIConf, scorer: Scoring, cutoff: Double, dirA: String, dirB: String, filter: Filter): Stream[List[(File, ParseTreeNode)]] = {
    val granularity = conf.granularity()

    val (parseTreeNodesA, parseTreeNodesB) = profile("Reading ASTs") {
      (getParseTrees(conf, dirA, filter), getParseTrees(conf, dirB, filter))
    }

    profile("Preprocessing") {
      val a = Array.ofDim[(Double, (File, ParseTreeNode))](parseTreeNodesB.size)

      var i = 0
      for (g ← parseTreeNodesB) {
        val score = scorer.selfSimilarity(g._2)
        a(i) = (score, g)
        i += 1
      }
      a.sortWith((x, y) ⇒ x._1 < y._1)

      val cmp = new java.util.Comparator[(Double, (File, ParseTreeNode))] {
        def compare(x: (Double, (File, ParseTreeNode)), y: (Double, (File, ParseTreeNode))): Int = math.signum(x._1 - y._1).toInt
      }

      def bsearch(arr:Array[(Double, (File, ParseTreeNode))], x: (Double, (File, ParseTreeNode))) = binarySearch[(Double, (File, ParseTreeNode))](arr, x, cmp)

      for {
        f ← parseTreeNodesA.toStream
        fscore = scorer.selfSimilarity(f._2)
        low = math.abs(bsearch(a, (fscore * cutoff + 1, f))) min (a.length-1)
        high = math.abs(bsearch(a, (fscore / cutoff - 1, f))) min (a.length-1)
        i ← low to high
        g = a(i)._2
      } yield List(f, g)
    }
  }

  def getParseTrees(conf: CLIConf, dir: String, filter: Filter): Seq[(File, ParseTreeNode)] = {
    val filesA = getFiles(conf, List(dir), filter)
    val granularity = conf.granularity()
    val readASTs = (fs: Seq[File]) ⇒ fs.flatMap(f ⇒ readAST(Some(conf), f, granularity).map(sexp => f -> sexp) )
    readASTs(filesA)
  }

  def main(args: Array[String]) {

    val conf = CLIConf(args)

    util.setLogLevel(if (conf.debug()) {
      util.Level.Debug
    } else {
      util.Level.Off
    })

    val scorer = conf.scoring()(conf.options)
    if (conf.subcommand == Some(conf.cutoff)) {
      assert(scorer.isInstanceOf[SmithWaterman], "cutoff-based optimization is supported by Smith-Waterman only")
    }

    var total = 0
    var shouldShowPercentage = false

    val filter = conf.filterFile.toOption.fold[Filter](AllPassFilter)(FileBasedFilter(_))

    val parseTreeNodes: Stream[List[(File, ParseTreeNode)]] = conf.subcommand match {
      case Some(conf.allpairs) ⇒ 
        val (tot, l) = allPairs(conf, conf.allpairs.files(), filter)
        total = tot
        shouldShowPercentage = true
        l.toStream
      case Some(conf.compare) ⇒ compareDirs(conf, conf.compare.a(), conf.compare.b(), filter)
      case Some(conf.cutoff) ⇒ cutoffPairs(conf, scorer, conf.cutoff.cutoff(), conf.cutoff.a(), conf.cutoff.b(), filter)
      case Some(subcommand) ⇒ sys.error(s"Unknown subcommand: $subcommand")
      case None ⇒ sys.error("No subcommand given")
    }

    List(ParseCPP, ParseGo, ParseJava, ParseJS).foreach(_.clearDFA())

    val reportFile = conf.report.toOption.map(new FileWriter(_))

    var i = 0

    val computeScores: List[(File, ParseTreeNode)] ⇒ List[((String, String), Double)] = {
      case List((a, parseTreeNodeA), (b, parseTreeNodeB)) ⇒ {

        val aname: String = a.getCanonicalPath + parseTreeNodeA.start + "-" + parseTreeNodeA.end
        val bname: String = b.getCanonicalPath + parseTreeNodeB.start + "-" + parseTreeNodeB.end

        if (filter contains (aname, bname)) {
          val result = scorer.similarity(parseTreeNodeA, parseTreeNodeB)

          this.synchronized {
            i += 1
            println(s"${aname}\t${bname}\t${result.score}")
            if (shouldShowPercentage) {
              println(s"$i / $total (" + i.doubleValue / total * 100.0 + "%)")
            }
          }

          reportFile foreach { output ⇒
            HTMLReporter.report(a, b, output, result)
          }

          List((aname, bname) → result.score, (bname, aname) → result.score)
        } else {
          List.empty
        }
      }
    }

    val memo: Map[(String, String), Double] = profile("Computing scores") {
      conf.jobs() match {
        case 1 ⇒ (parseTreeNodes flatMap computeScores).toMap
        case nJobs if nJobs > 1 ⇒ {
          val es = Executors.newFixedThreadPool(nJobs)
          implicit val ctx = ExecutionContext.fromExecutor(es)
          val future = Future.traverse(parseTreeNodes)(s ⇒ Future(computeScores(s))(ctx)).collect(PartialFunction.apply(_.flatten.toMap))
          val result = Await.result(future, Duration.Inf)
          es.shutdown()
          result
        }
        case nJobs ⇒ sys.error(s"Number of threads given with --jobs must be greater than 0 but $nJobs is given!")
      }
    }

    val names = (memo.keySet.map(_._1) ++ memo.keySet.map(_._2)).toList.sorted.distinct
    println(s"Compared ${memo.size / 2} pairs")
    val (writeToFile, csvFileName) = conf.writeToCSV.toOption match {
      case Some(fname) => (true, fname)
      case None => (false, "")
    }

    val csvFile = if (writeToFile) Some(new FileWriter(csvFileName)) else None
    def printAndWrite(s: String, newline: Boolean = true) {
      /*if (newline) println(s)
      else print(s)*/
      csvFile match {
        case Some(fw) =>
          if (newline) { fw.write(s); fw.write("\n") }
          else fw.write(s)
        case None => ()
      }
    }
    def closeCSVFile() = {
      csvFile match {
        case Some(fw) => fw.close()
        case None => ()
      }
    }

    printAndWrite("," + names.mkString(","))

    for (name <- names) {
      printAndWrite(name + ",", false)
      for (name2 <- names) {
        val res = if (name == name2) 1.0 else memo.getOrElse((name, name2), -1.0)
        printAndWrite(res + ",", false)
      }
      printAndWrite("")
    }

    closeCSVFile()
  }

  def printResult[A](r: Result[A]): Unit = {
    println("-" * 80)
    println(s"Similarity score: ${r.score}\n")
    println(s"Matches (line numbers):")
    for (Match(Interval(aBegin, aEnd), Interval(bBegin, bEnd)) ← r.matches) {
      println(s"${aBegin} to ${aEnd}\tmatch\t${bBegin} to ${bEnd}")
    }
  }

  /** Read AST from given file */
  def readAST(conf: Option[CLIConf], file: File, granularity: String = "file"): List[ParseTreeNode] = {
    import fett.parsing._

    // println("parsing " + file.getCanonicalPath())
    val extension = file.getCanonicalPath().split('.').tail.last.toLowerCase

    val scorer = conf.map(conf ⇒ conf.scoring()(conf.options))

    var isCpp = false

    val parse = extension match {
      case "c" ⇒ ParseC.parse _
      case "cpp" | "h" | "hpp" | "cxx" | "hxx" | "cc" | "hh" ⇒ isCpp = true; ParseCPP.parse _
      case "java" ⇒ ParseJava.parse _
      case "go" ⇒ ParseGo.parse _
      case "js" ⇒ ParseJS.parse _
      case _ ⇒ sys.error(file.getCanonicalPath() + ": lang needs to be one of {c, cpp, java, js, go}")
    }

    val tree = try {
      val ptree = ParsingHelpers.toParseTree(parse(file.getCanonicalPath))
      // collapse the tree if scoring method needs collapsed trees
      val t = scorer match {
        case Some(s) if s.collapseTrees ⇒ {
          ptree//.collapse(s.importantNodeLabels)
        }
        case _ ⇒ ptree
      }
      Some(t)
    } catch {
      case e: Exception ⇒
        println(s"Error parsing file $file:")
        println(e.getMessage)
        None
    }

    val sortFunctionsInFileGranularity = scorer match {
      case Some(_: Zhang13SmithWaterman) => false
      case otherwise => true
    }

    val res = granularity match {
      case "file" ⇒ if (!sortFunctionsInFileGranularity) tree.toList.map(_.root)
                    else {
                      val newChildren = tree.fold(IndexedSeq.empty[ParseTreeNode])(_.functionNodes.toIndexedSeq).sortBy(_.size)
                      List(ParseTreeNode("__Dummy_Root", newChildren))
                    }

      case "function" ⇒ tree.fold(List.empty[ParseTreeNode])(_.functionNodes)
    }

    res.filter(t => {
      val newt = scorer match {
        case Some(s) if s.collapseTrees && isCpp => 
          t.collapse(s.importantNodeLabels)
        case _ => t
      }
      scorer.get.filterParseTreeNode(newt)
    }).map(t => {scorer match {
        case Some(s) if s.collapseTrees && isCpp => 
          t.collapse(s.importantNodeLabels)
        case _ => t
    }})
  }
}
