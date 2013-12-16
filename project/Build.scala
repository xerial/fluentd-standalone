import sbt._
import sbt.Keys._

object Build extends sbt.Build {

  val copyFluentd = TaskKey[Unit]("copy-fluentd", "Embed fluend into jar")

  lazy val root = Project(
    id = "fluentd-standalone",
    base = file("."),
    settings = Defaults.defaultSettings ++ 
      Seq(
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
        // custom settings here
        scalaVersion := "2.10.3",
        crossPaths := false,
        packageBin in Compile <<= (packageBin in Compile).dependsOn(copyFluentd),
        copyFluentd := {
          val baseDir : File = baseDirectory.value
          val fluentd = baseDir / "fluentd"
          val targetDir : File = target.value / "classes/xerial/fluentd/core"
          val s = streams.value
          def rpath(path:File) =  path.relativeTo(baseDir).getOrElse(path)

          s.log.info("copy " + rpath(fluentd) + " to " + rpath(targetDir))
            val p = (fluentd ** "*")
            for(file <- p.get; relPath <- file.relativeTo(fluentd) if !relPath.getPath.startsWith(".git")) {
              val out = targetDir / relPath.getPath
              if(file.isDirectory) {
                s.log.debug("create directory: " + rpath(out))
                IO.createDirectory(out)
              }
              else {
                s.log.debug("copy " + rpath(file) + " to " + rpath(out))
                IO.copyFile(file, out, preserveLastModified=true)
              }
          }
        },
        libraryDependencies ++= Seq(
          // Add dependent jars here
          "org.xerial" % "xerial-core" % "3.2.2",
          "org.fusesource.scalate" % "scalate-core_2.10" % "1.6.1",
          "org.slf4j" % "slf4j-simple" % "1.7.5" % "test",
          "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
        )
      )
  )
}
