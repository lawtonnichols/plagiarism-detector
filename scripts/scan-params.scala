#!/usr/bin/env scala

import java.io._
import java.nio.file._
import java.util.Properties
import scala.collection.JavaConverters.propertiesAsScalaMapConverter
import scala.sys.process._

object ScanParameters {
  def main(args: Array[String]): Unit = {
    val (ex, paramFile, testDir, outputDir, filterFile) = args match {
      case Array(e, p, t, o, f) ⇒
        val pf = new File(p)
        assert(pf.exists() && pf.isFile(), s"parameter file '$p' is not a file or it doesn't exist!")
        val td = new File(t)
        assert(td.exists() && td.isDirectory(), s"test directory '$t' is not a directory or it doesn't exist!")
        val od = new File(o)
        assert(od.exists() && od.isDirectory(), s"output directory '$o' is not a directory or it doesn't exist!")
        val ff = new File(f)
        assert(ff.exists() && ff.isFile(), s"parameter file '$f' is not a file or it doesn't exist!")
        (e, pf, t, od, Some(f))
      case Array(e, p, t, o) ⇒
        val pf = new File(p)
        assert(pf.exists() && pf.isFile(), s"parameter file '$p' is not a file or it doesn't exist!")
        val td = new File(t)
        assert(td.exists() && td.isDirectory(), s"test directory '$t' is not a directory or it doesn't exist!")
        val od = new File(o)
        assert(od.exists() && od.isDirectory(), s"output directory '$o' is not a directory or it doesn't exist!")
        (e, pf, t, od, None)
      case _ ⇒
        println("Usage: scan-parameters executable parameter-file test-directory output-directory [filter-file]")
        sys.exit(1)
    }

    val (matchScores, gapScores, classFiles, cutoff, algorithm) = readParamFile(paramFile)
    println(matchScores)
    println(gapScores)
    println(classFiles)
    runExperiments(ex, matchScores, gapScores, classFiles, testDir, outputDir, filterFile, cutoff, algorithm)
  }

  def runExperiments(executable: String,
                     matchScores: Seq[String],
                     gapScores: Seq[String],
                     classFiles: Seq[String],
                     testDir: String,
                     outputDir: File,
                     filterFile: Option[String],
                     cutoff: String,
                     algorithm: String) = {
    val filter = filterFile.fold(Seq.empty[String])(Seq("--pair-filter", _))
    for {
      m ← matchScores
      g ← gapScores
      c ← classFiles
    } {
      val cf = new File(c).getName
      val fileName = s"test_${algorithm}_${m}_${g}_${cf}"
      val cmd = Seq("java", "-Xmx16G", "-jar", executable, "-s", algorithm, "-r", "-g", "function", "-X", s"cutoff=$cutoff", "similarity=classBased", s"matchScore=$m", s"gapScore=$g", s"classFile=$c", "-c", new File(outputDir, fileName + ".csv").getCanonicalPath, "-j6") ++ filter ++ Seq("allpairs", testDir)
      val proc = cmd #> new File(outputDir, fileName + ".log")
      // println(cmd.mkString(" "))
      val retCode = proc.!
      if (retCode == 0) {
        println(s"${m}\t${g}\t${c}\tsuccess\t0")
      } else {
        println(s"${m}\t${g}\t${c}\tfail\t$retCode")
      }
    }
  }

  def stringValues(v: String): Seq[String] = {
    genericArrayOps(v.split(",")).toSeq.map((x:String) ⇒ x.trim).distinct
  }

  def readParamFile(f: File) = {
    val props = new Properties()
    val fis = new InputStreamReader(new FileInputStream(f), "UTF-8")
    props.load(fis)
    fis.close()
    val m = props.asScala
    val matchScores = stringValues(m("matchScores"))
    val gapScores = stringValues(m("gapScores"))
    val classFiles = stringValues(m("classFiles"))
    val cutoff = m("cutoff")
    val algorithm = m("algorithm")
    (matchScores, gapScores, classFiles, cutoff, algorithm)
  }
}
