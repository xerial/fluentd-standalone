import sbt._
import sbt.Keys._

object Build extends sbt.Build {

  lazy val root = Project(
    id = "fluentd-standalone",
    base = file("."),
    settings = Defaults.defaultSettings ++ 
      Seq(
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
        // custom settings here
        scalaVersion := "2.10.3",
        crossPaths := false,
        libraryDependencies ++= Seq(
          // Add dependent jars here
          "org.xerial" % "xerial-core" % "3.2.2",
          "org.fusesource.scalate" % "scalate-core_2.10" % "1.6.1",
          "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
        )
      )
  )
}
