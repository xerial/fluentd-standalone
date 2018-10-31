import sbt.Keys.resolvers
import sbt.url

val copyFluentd   = taskKey[Unit]("Embed fluentd into jar")
val SCALA_VERSION = "2.12.7"

lazy val root = Project(id = "fluentd-standalone", base = file("."))
  .settings(
    organization := "org.xerial",
    organizationName := "xerial.org",
    organizationHomepage := Some(new URL("http://github.com/xerial/fluentd-standalone")),
    description := "Standalone fluentd server for Java and Scala",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    // custom settings here
    scalaVersion := SCALA_VERSION,
    crossScalaVersions := Seq("2.11.11", SCALA_VERSION, "2.13.0-M5"),
    crossPaths := true,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    compile in Compile := { (compile in Compile).dependsOn(copyFluentd).value },
    copyFluentd := {
      val baseDir: File = baseDirectory.value
      val fluentd       = baseDir / "fluentd"

      val targetDir: File   = (classDirectory in Compile).value / "xerial/fluentd/core"
      val s                 = streams.value
      def rpath(path: File) = path.relativeTo(baseDir).getOrElse(path)

      s.log.info("copy " + rpath(fluentd) + " to " + rpath(targetDir))
      val p = (fluentd ** "*")
      for (file <- p.get; relPath <- file.relativeTo(fluentd) if !relPath.getPath.startsWith(".git")) {
        val out = targetDir / relPath.getPath
        if (file.isDirectory) {
          s.log.debug("create directory: " + rpath(out))
          IO.createDirectory(out)
        } else {
          s.log.debug("copy " + rpath(file) + " to " + rpath(out))
          IO.copyFile(file, out, preserveLastModified = true)
        }
      }
    },
    logBuffered in Test := false,
    libraryDependencies ++= Seq(
      // Add dependent jars here
      "org.wvlet.airframe" %% "airframe-log"     % "0.72",
      "org.wvlet.airframe" %% "airframe-control" % "0.72",
      "org.slf4j"          % "slf4j-simple"      % "1.7.22" % "test",
      "org.scalatest"      %% "scalatest"        % "3.0.6-SNAP4" % "test"
    ),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/xerial/fluentd-standalone")),
    scmInfo := Some(
      ScmInfo(
        browseUrl = url("https://github.com/xerial/fluentd-standalone"),
        connection = "scm:git@github.com:xerial/fluentd-standalone.git"
      )
    ),
    developers := List(
      Developer(id = "leo", name = "Taro L. Saito", email = "leo@xerial.org", url = url("http://xerial.org/leo"))
    ),
    // Release settings
    publishTo := sonatypePublishTo.value,
    // Use sonatype resolvers
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    )
  )
