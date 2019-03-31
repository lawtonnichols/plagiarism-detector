#!/usr/bin/env scala

import java.io._

object Main extends App {

  def getFileName(fn: String): String = {
    // trick to keep header
    if (fn contains '.')
      new File(fn.split(":")(0)).getName.reverse.dropWhile('.' != _).tail.reverse.toLowerCase
    else
      fn
  }

  val csvFile = io.Source.fromFile(new File(args(0))).getLines

  for {
    row ← csvFile
    a :: b :: rest = row.split(",").toList.map(_.trim) if getFileName(a) == getFileName(b)
  }{
      println((a :: b :: rest).mkString(","))
  }
}
