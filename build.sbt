// to deploy a self-executable binary
import sbtassembly.AssemblyPlugin.defaultShellScript

name := "fett"

version := "0.1"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-unchecked",
                      "-deprecation",
                      "-feature",
                      "-language:postfixOps",
                      "-language:implicitConversions",
                      "-opt:_"
)//, "-Xdisable-assertions")

// Add Sonatype OSS repositories for ScalaTest and ScalaCheck
resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  // scallop, for command-line argument parsing
  "org.rogach" %% "scallop" % "2.0.5",
  // logging dependencies
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  // parser combinators
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
  // antlr
  "org.antlr" % "antlr4-runtime" % "4.6"
)

// testing dependencies: ScalaTest and ScalaCheck
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)

// compile Java first for faster compilation
compileOrder := CompileOrder.JavaThenScala

// create a self-executable binary by `sbt assembly` command:
assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript))

assemblyJarName in assembly := s"${name.value}"

mainClass in assembly := Some("fett.CloneDetection")

// Disable parallel execution of tests
parallelExecution in Test := false

// show durations.
testOptions in Test ++= Seq(Tests.Argument("-oD"), Tests.Argument("-l"), Tests.Argument("Concrete"))

