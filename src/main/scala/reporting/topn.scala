package fett.reporting

import fett.scoring._
import fett.scoring.Scoring.LineNo
import java.io._
import scala.collection.mutable.{MutableList => MList, Set => MSet, Map => MMap}
import scala.util._
import fett.CLIConf

class TopNHTMLOutput(memo: Map[(String, String), Double], conf: CLIConf) {
  type FileNameAndFunctionLocation = String
  type Score = Double
  def extractFileName(s: FileNameAndFunctionLocation): String = {
    val r = """([^:]*):(\d+):\d+-:(\d+):\d+""".r
    s match {
      case r(filename, startline, endline) => filename
    }
  }
  def getExtension(s: String): String = {
    val res = s.reverse.takeWhile(_ != '.').reverse.toLowerCase
    res
  }
  def createFunctionToTopNResultsMap(memo: Map[(FileNameAndFunctionLocation, FileNameAndFunctionLocation), Double], n: Int): Map[FileNameAndFunctionLocation, List[(FileNameAndFunctionLocation, Score)]] = {
    val functions: Set[FileNameAndFunctionLocation] = memo.keys.map(_._1).toSet
    val m = MMap[FileNameAndFunctionLocation, List[(FileNameAndFunctionLocation, Score)]]()
    for (function1 <- functions) {
      val otherFileNamesAndScores: Seq[(FileNameAndFunctionLocation, Double)] = {
        for {function2 <- functions} yield (function2, if (function1 == function2) 1.0 else memo((function1, function2)))
      }.toList.sortBy(_._2).reverse

      def isCPP(filename: String): Boolean = getExtension(extractFileName(filename)).toLowerCase match {
        case "cpp" | "c" | "h" | "hpp" | "cxx" | "hxx" | "cc" | "hh" => true
        case _ => false
      }
      def isJava(filename: String): Boolean = getExtension(extractFileName(filename)).toLowerCase match {
        case "java" => true
        case _ => false
      }
      def isJS(filename: String): Boolean = getExtension(extractFileName(filename)).toLowerCase match {
        case "js" => true
        case _ => false
      }
      def isGo(filename: String): Boolean = getExtension(extractFileName(filename)).toLowerCase match {
        case "go" => true
        case _ => false
      }

      val topCPP = otherFileNamesAndScores.filter(fs => isCPP(fs._1)).take(n)
      val topJava = otherFileNamesAndScores.filter(fs => isJava(fs._1)).take(n)
      val topJS = otherFileNamesAndScores.filter(fs => isJS(fs._1)).take(n)
      val topGo = otherFileNamesAndScores.filter(fs => isGo(fs._1)).take(n)
      m(function1) = topCPP.toList ++ topJava ++ topJS ++ topGo
    }

    m.toMap
  }
  def createFunctionToFirstNSourceLinesMap(memo: Map[(String, String), Double], n: Int): Map[FileNameAndFunctionLocation, String] = {
    val fileNamesStartLinesEndLines: Map[FileNameAndFunctionLocation, (String, Int, Int)] = memo.keys.map(_._1).toSet.map((s: String) => {
                                                                                                                            val r = """([^:]*):(\d+):\d+-:(\d+):\d+""".r
                                                                                                                            s match {
                                                                                                                              case r(filename, startline, endline) => s -> (filename, startline.toInt, endline.toInt)
                                                                                                                            }
                                                                                                                          }).toMap

    fileNamesStartLinesEndLines.mapValues({ case (filename, startline, endline) => {
                                             scala.io.Source.fromFile(filename).getLines.drop(startline-1).take(endline - startline + 1).toList.mkString("\n")
                                           } })
  }

  val htmlFileNameO = conf.topNHTML.toOption
  val topN = conf.topN()
  if (htmlFileNameO.nonEmpty) {
    val functionToFirstNSourceLinesMap: Map[FileNameAndFunctionLocation, String] = createFunctionToFirstNSourceLinesMap(memo, 5)
    val functionToTopNResultsMap: Map[FileNameAndFunctionLocation, List[(FileNameAndFunctionLocation, Score)]] = createFunctionToTopNResultsMap(memo, topN)
    var f = new FileWriter(htmlFileNameO.get)
    assert(htmlFileNameO.get.endsWith(".html"))

    val htmlhead = """
      <html>
      <head><title>Results</title>
      <meta charset="UTF-8"> 
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
      <script>
      $(document).ready(function () {
          $("h1").click(function () {
               $(this).next().toggle();
          });

          $("h2").click(function () {
               $(this).next().toggle();
          });

          $("div").hide();
      });
      </script>
      <style>
      h1 {
        font-size: 18pt;
      }
      h2 {
        font-size: 13pt;
      }
      .cpp, .h, .hpp, .cxx, .hxx, .cc, .hh {color: red;}
      .java {color: orange;}
      .js {color: green;}
      .go {color: blue;}
      </style>
      <body>
      """

    val htmltail = """
      </body>
      </html>
      """

    f.write(htmlhead)

    var f_i = 0
    var currentFileName = htmlFileNameO.get
    val FILES_PER_PAGE = 50
    for (function <- functionToTopNResultsMap.keys.toList.sorted) {
      // print the function
      f.write(s"<h1>$function</h1>\n")
      val functiontext = functionToFirstNSourceLinesMap(function).replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>\n").replace("\t", "   ").replace(" ", "&nbsp;")
      // wrap everything inside a div
      f.write("<div>\n")
      f.write(s"<tt>$functiontext</tt>\n")
      
      // print the top n results per language for this function
      for (result <- functionToTopNResultsMap(function)) {
        val (function2, score) = result
        val extension = getExtension(extractFileName(function2))
        val function2text = functionToFirstNSourceLinesMap(function2).replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>\n").replace("\t", "   ").replace(" ", "&nbsp;")
        f.write(s"""<h2 class="$extension">$function2 ($score)</h2>\n""")
        f.write(s"<div>\n")
        f.write(s"<tt>$function2text</tt>\n")
        f.write(s"</div>\n")
      }
      f.write("</div>\n")

      f_i += 1
      if (f_i % FILES_PER_PAGE == 0) {
        val nextfilenumber = f_i / FILES_PER_PAGE + 1
        val newfilename = htmlFileNameO.get.reverse.dropWhile(_ != '.').reverse.init + (nextfilenumber) + ".html"
        val filenamebase = new File(htmlFileNameO.get).getName.reverse.dropWhile(_ != '.').reverse.init
        // close the current file and start a new one
        // add prev link
        if (f_i > FILES_PER_PAGE) {
          val prevfilename = if (nextfilenumber == 3) s"$filenamebase.html" else s"$filenamebase${nextfilenumber-2}.html"
          f.write(s"""
            <a href="$prevfilename">Prev</a>&nbsp;&nbsp;
            """)
        }
        // add next link
        f.write(s"""
            <a href="$filenamebase$nextfilenumber.html">Next</a>
            """)
        f.write(htmltail)
        f.close()
        f = new FileWriter(newfilename)
        currentFileName = newfilename
        println(s"now writing to $newfilename")
        f.write(htmlhead)
      }
    }

    if (currentFileName != htmlFileNameO.get) {
      // add prev link
      val currentfilenumber = f_i / FILES_PER_PAGE + 1
      val filenamebase = new File(htmlFileNameO.get).getName.reverse.dropWhile(_ != '.').reverse.init
      if (f_i > FILES_PER_PAGE) {
        val prevfilename = if (currentfilenumber == 2) s"$filenamebase.html" else s"$filenamebase${currentfilenumber-1}.html"
        f.write(s"""
          <a href="$prevfilename">Prev</a>&nbsp;&nbsp;
          """)
      }
    }
    f.write(htmltail)

    f.close()
  }
}
