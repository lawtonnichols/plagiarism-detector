package fett.reporting

import fett.scoring.Result
import fett.scoring.Scoring.LineNo
import java.io.File

trait Reporter {
  def report(a: File, b: File, output: File, r: Result[LineNo]): Unit
}
