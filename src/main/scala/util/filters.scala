package fett.util

import java.nio.file._
import java.io._
import scala.collection.JavaConverters._

trait Filter {
  /** check if this filter contains a function */
  def containsFn(sig: String): Boolean
  /** check if this filter contains a function pair */
  def contains(pair: (String, String)): Boolean
  /** check if this filter contains a file */
  val fileFilter: FilenameFilter
}

object AllPassFilter extends Filter {
  def containsFn(sig: String) = true

  def contains(pair: (String, String)) = true
  val fileFilter = new FilenameFilter {
    def accept(dir: File, fName: String) = true
  }
}

case class FileBasedFilter(f: File) extends Filter {

  val pairs = (for (row ← Files.readAllLines(f.toPath).asScala) yield {
    val p = row.split(",").take(2)
    List((p(0), p(1)), (p(1), p(0)))
  }).flatten.toSet

  private def getFileName(fn: String): String = {
    fn.split(":", 2).head
  }

  val files = pairs.flatMap(p ⇒ List(getFileName(p._1), getFileName(p._2))).toSet

  val fns = pairs.map(_._1) ++ pairs.map(_._2)

  def containsFn(sig: String) = fns contains sig
  def contains(pair: (String, String)) = pairs contains pair
  val fileFilter = new FilenameFilter {
    def accept(dir: File, fName: String) = {
      files contains (new File(dir, fName)).getCanonicalPath
    }
  }
}

object Implicits {
  implicit class FilenameFilterOp(f: FilenameFilter) {
    def &(g: FilenameFilter) = new FilenameFilter {
      def accept(dir: File, fName: String) = f.accept(dir, fName) && g.accept(dir, fName)
    }
    def |(g: FilenameFilter) = new FilenameFilter {
      def accept(dir: File, fName: String) = f.accept(dir, fName) || g.accept(dir, fName)
    }
    def unary_~ = new FilenameFilter {
      def accept(dir: File, fName: String) = ! f.accept(dir, fName)
    }
  }
}
