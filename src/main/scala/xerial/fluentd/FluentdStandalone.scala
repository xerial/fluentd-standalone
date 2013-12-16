//--------------------------------------
//
// FluentdStandalone.scala
// Since: 2013/12/16 10:48 AM
//
//--------------------------------------

package xerial.fluentd

import xerial.core.io.{Resource, IOUtil}
import java.io._
import xerial.core.log.Logger

import xerial.core.io.Path._
import org.fusesource.scalate.TemplateEngine
import xerial.core.util.Shell
import xerial.core.io.Resource.VirtualFile

/**
 * @author Taro L. Saito
 */
object FluentdStandalone extends Logger {

  def start(config:FluentdConfig = FluentdConfig()) : FluentdStandalone = {
    val fd = new FluentdStandalone(config)
    fd.start
    fd
  }

  def defaultConfig : String =
    """
      |## Forward all log messages to stdout
      |<match **>
      |  type stdout
      |</match>
      |
    """.stripMargin

}


/**
 * Fluentd configuration
 * @param port port number to listen
 * @param configFile if null, the default configuration file is generated to (workdir)/fluent.conf
 * @param workDir working directory. default is target/fluentd
 * @param configuration fluentd configuration script used when configFile is null. The default configuration just outputs every log to stdout.
 */
case class FluentdConfig(port:Int = IOUtil.randomPort,
                         configFile:String=null,
                         workDir:String = "target/fluentd",
                         configuration : String = FluentdStandalone.defaultConfig) {


  def getConfigFile : File = if(configFile == null) {
    workDir / "fluent.conf"
  }
  else
    new File(configFile)


  def fluentdCmd = s"${workDir}/core/bin/fluentd"
  def fluentCatCmd = s"${workDir}/core/bin/fluent-cat"

}



class FluentdStandalone(val config:FluentdConfig) extends Logger {

  def this(port:Int) = this(FluentdConfig(port = port))

  private var fluentdProcess : Option[Process] = None


  def port : Int = config.port

  /**
   * Start fluentd and returns fluentd port number
   * @return
   */
  def start : Int = {
    prepare(config)

    info(s"Launching fluentd")
    val process = Shell.launchProcess(s"${config.fluentdCmd} -c ${config.getConfigFile}")
    fluentdProcess = Some(process)
    val t = new Thread(new Runnable {
      def run() {
        process.waitFor()
        val ret = process.exitValue
        if(ret != 0) {
          error(s"Error occurred while launching fluentd (error code:$ret). If you see 'LoadError', install fluentd and its dependencies by 'gem install fluentd'")
        }
      }
    })
    t.setDaemon(true)
    t.start()

    config.port
  }


  def stop {
    fluentdProcess.map { p =>
      info(s"Terminating fluentd")
      p.destroy()
    }
  }


  private[fluentd] def prepare(config:FluentdConfig) = {

    def mkdir(path:File) {
      path.mkdirs()
      if(!path.exists())
        throw new IOException(s"Failed to create directory: ${path}")
    }

    // Create workdir

    val workDir = new File(config.workDir)
    val coreDir = workDir / "core"
    val pluginDir = workDir / "plugin"
    debug(s"Creating fluentd workdir: $workDir")
    mkdir(workDir)
    mkdir(coreDir)
    mkdir(pluginDir)

    // Copy fluentd
    for(f <- Resource.listResources("xerial.fluentd.core")) {

      def relPath(f:VirtualFile) = f.logicalPath.replaceFirst("xerial/fluent/core/", "")
      val rpath = relPath(f)
      val targetPath = coreDir / rpath

      if(f.isDirectory)
        targetPath.mkdirs()
      else {
        IOUtil.withResource(f.url.openStream) { r =>
          IOUtil.readFully(r) { fileContents =>
            targetPath.getParentFile().mkdirs()
            IOUtil.withResource(new FileOutputStream(targetPath)) { out =>
              out.write(fileContents)
            }

            if(rpath.startsWith("bin"))
              targetPath.setExecutable(true)
          }
        }
      }
    }

    // Create fluent.conf if it doesn't specified
    if(config.configFile == null) {
      val targetFile = config.getConfigFile
      // Create fluent.conf from a template
      val prop = Map[String, Any]("port" -> config.port.toString, "configuration" -> config.configuration)
      val engine = new TemplateEngine
      val configText = engine.layout("/xerial/fluentd/fluent.conf.mustache", prop)
      // Write fluent.conf
      IOUtil.withResource(new FileWriter(targetFile)) { writer =>
        writer.write(configText)
      }
    }
  }

}