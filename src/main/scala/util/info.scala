package fett.util

package object info {
  val version: String = getClass.getPackage.getImplementationVersion
  // assert(version != null, "Version string is not defined")
  // ^ commented out this line so that I can test without exiting sbt
  // hopefully nothing gets broken
}
