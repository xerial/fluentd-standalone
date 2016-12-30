import sbt._
import sbt.Keys._

object Build extends sbt.Build {

  private def profile = System.getProperty("xerial.profile", "default")

  def releaseResolver(v: String): Option[Resolver] = {
    profile match {
      case "default" => {
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      }
      case p => {
        scala.Console.err.println("unknown xerial.profile '%s'".format(p))
        None
      }
    }
  }

  val copyFluentd = TaskKey[Unit]("copy-fluentd", "Embed fluend into jar")

  val SCALA_VERSION = "2.12.1"

  lazy val root = Project(
    id = "fluentd-standalone",
    base = file("."),
    settings = Defaults.defaultSettings ++ sbtrelease.ReleasePlugin.releaseSettings ++
      Seq(
        organization := "org.xerial",
        organizationName := "xerial.org",
        organizationHomepage := Some(new URL("http://github.com/xerial/fluentd-standalone")),
        description := "Standalone fluend server for Java and Scala",
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
        // custom settings here
        scalaVersion := SCALA_VERSION,
        crossScalaVersions := Seq("2.10.5", "2.11.8", SCALA_VERSION),
        crossPaths := true,
        publishMavenStyle := true,
        publishArtifact in Test := false,
        publishTo <<= version { v => releaseResolver(v) },
        compile in Compile <<= (compile in Compile).dependsOn(copyFluentd),
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
        logBuffered in Test := false,
        libraryDependencies ++= Seq(
          // Add dependent jars here
          "org.xerial" %% "xerial-core" % "3.6.0",
          "org.slf4j" % "slf4j-simple" % "1.7.22" % "test",
          "org.scalatest" %% "scalatest" % "3.0.1" % "test"
        ),
        pomExtra := {
          <url>http://xerial.org/</url>
            <licenses>
              <license>
                <name>Apache 2</name>
                <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
              </license>
            </licenses>
            <scm>
              <connection>scm:git:github.com/xerial/fluentd-standalone.git</connection>
              <developerConnection>scm:git:git@github.com:xerial/fluentd-standalone.git</developerConnection>
              <url>github.com/xerial/silk.git</url>
            </scm>
            <properties>
              <scala.version>{SCALA_VERSION}</scala.version>
              <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            </properties>
            <developers>
              <developer>
                <id>xerial</id>
                <name>Taro L. Saito</name>
                <url>http://xerial.org/leo</url>
              </developer>
            </developers>
        }

      )
  )
}
