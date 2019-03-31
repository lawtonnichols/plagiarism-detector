package fett.reporting

import fett.scoring._
import fett.scoring.Scoring.LineNo
import java.io._

object HTMLReporter {
  val colors = "#ffaaaa #aaffaa #aaaaff #ffff55 #ff55ff #55ffff".split(' ')

  def head(a: String, b: String): String = {
    val head1 =      s"""|<html>
                         |  <head>
                         |    <style>
                         |      body, html, tr, pre {
                         |      height: 100%;
                         |      }
                         |      .code {
                         |      height: 90vh;
                         |      width: 40vw;
                         |      margin-left: auto;
                         |      overflow: scroll;
                         |      font-size: 1.2em;
                         |      }
                         |""".stripMargin

    val head2 =      s"""|
                         |      .code a {
                         |      color: inherit;
                         |      text-decoration: none;
                         |      }
                         |    </style>
                         |  </head>
                         |  <body>
                         |    <h1>Comparison of $a and $b</h1>
                         |
                         |    <table>
                         |      <tr>
                         |        <th>$a</th>
                         |        <th>$b</th>
                         |      </tr>
                         |      <tr>
                         |        <td>
                         |          <div class="code" id="a">
                         |          <pre>""".stripMargin

    val h = new StringBuilder()
    h.append(head1)
    for (color ← colors) {
      h.append(s"""|      .match-1 {
                   |      background-color: $color;
                   |      }""".stripMargin)
    }
    h.append(head2)

    h.toString
  }

  val separator = """|
                     |          </pre>
                     |          </div>
                     |        </td>
                     |        <td>
                     |          <div class="code" id="b">
                     |            <pre>""".stripMargin

  val foot = """|
                |          </pre>
                |          </div>
                |        </td>
                |      </tr>
                |    </table>
                |  </body>
                |</html>
                |""".stripMargin

  def linesOf(f: File) = {
    val reader = new LineNumberReader(new FileReader(f))

    def lines: Stream[String] = {
      val lineNo = reader.getLineNumber
      val line = reader.readLine
      if (line == null) {
        reader.close()
        return Stream.empty
      }
      line #:: lines
    }

    lines
  }

  def report(a: File, b: File, out: Writer, r: Result[LineNo]): Unit = {
    val matches = r.matches.toSeq zip Stream.from(1) sortBy(_._1.left.begin)

    // TODO: Make this faster using an interval tree to find matches
    val linesOfA = linesOf(a).toArray

    out.write(head(a.getName, b.getName))

    for ((Match(Interval(begin, end), _), i) ← matches if begin != -1 && end != -1) {
      for (j ← begin to end) {
        linesOfA(j) = s"""<a href="#match-$i-b" id="match-$i-a" class="match-${i % colors.length}">${linesOfA(j)}</a>"""
      }
    }

    for (line ← linesOfA) {
      out.write(line)
      out.write('\n')
    }

    out.write(separator)

    val linesOfB = linesOf(b).toArray

    for ((Match(Interval(begin, end), _), i) ← matches) {
      for (j ← begin to end) {
        linesOfB(j) = s"""<a href="#match-$i-a" id="match-$i-b" class="match-${i % colors.length}">${linesOfB(j)}</a>"""
      }
    }

    for (line ← linesOfB) {
      out.write(line)
      out.write('\n')
    }

    out.write(foot)
    out.close()
  }
}
