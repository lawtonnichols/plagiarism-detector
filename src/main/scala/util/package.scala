package fett

import com.typesafe.scalalogging._
import ch.qos.logback.{classic ⇒ logback}

package object util {

  val logger = Logger("fett")

  sealed trait Level

  object Level {
    case object Off extends Level
    case object Debug extends Level
  }

  def setLogLevel(level: Level): Unit = {
    import Level._

    val lvl = level match {
      case Off ⇒ logback.Level.OFF
      case Debug ⇒ logback.Level.DEBUG
    }

    val logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[logback.Logger]
    logger.setLevel(lvl)
  }

  lazy val identifierNodeNames: Set[String] = {
    val sourceFile = io.Source.fromURL(getClass.getResource("/identifierNodeNames.txt"))
    sourceFile.getLines.map("antlrparsers." + _ + "Context").toSet
  }
}
