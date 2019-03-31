package fett.util

import java.time.Instant
import java.time.temporal.ChronoUnit

/** Lightweight profiling methods to profile certain parts of code.
  */
package object profiling {
  @inline def profile[A](s: String)(f: ⇒ A): A = {
    println(s"[profiler] $s")
    val begin = Instant.now
    val result = f
    val end = Instant.now
    // try to get millisecond precision but report in seconds for readability
    val time: Double = begin.until(end, ChronoUnit.MILLIS) / 1e3
    println(f"[profiler] Elapsed time: $time%.3f s")
    result
  }
}
