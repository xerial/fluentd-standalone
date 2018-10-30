//--------------------------------------
//
// FluentdStandalone.scala
// Since: 2013/12/16 10:48 AM
//
//--------------------------------------

package xerial.fluentd

import java.io._

import wvlet.log.LogSupport
import java.net.Socket

import wvlet.airframe.control.Shell
import wvlet.log.io.Resource.VirtualFile
import wvlet.log.io.{IOUtil, Resource}

/**
  * @author Taro L. Saito
  */
object FluentdStandalone extends LogSupport {

  def start(config: FluentdConfig = FluentdConfig()): FluentdStandalone = {
    val fd = new FluentdStandalone(config)
    fd.startAndAwait
    fd
  }

  def defaultConfig: String =
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
case class FluentdConfig(port: Int = IOUtil.unusedPort,
                         configFile: String = null,
                         initialWaitMilliSeconds: Int = 500,
                         maxWaitMilliSeconds: Int = 10000,
                         workDir: String = "target/fluentd",
                         configuration: String = FluentdStandalone.defaultConfig) {

  def getConfigFile: File =
    if (configFile == null) {
      new File(workDir, "fluent.conf")
    } else
      new File(configFile)

  def fluentdCmd   = s"${workDir}/core/bin/fluentd"
  def fluentCatCmd = s"${workDir}/core/bin/fluent-cat"

}

class FluentdStandalone(val config: FluentdConfig) extends LogSupport {

  def this(port: Int) = this(FluentdConfig(port = port))

  private var fluentdProcess: Option[Process] = None

  def port: Int = config.port

  /**
    * Start fluentd and returns fluentd port number
    * @return
    */
  def start: Int = {
    prepare(config)

    info(s"Starting fluentd")
    val process = Shell.launchProcess(s"${config.fluentdCmd} -c ${config.getConfigFile} --no-supervisor")
    fluentdProcess = Some(process)
    val t = new Thread(new Runnable with LogSupport {
      def run() {
        process.waitFor()
        val ret = process.exitValue
        ret match {
          case 0   => // OK
          case 143 => // terminated by stop() (SIGTERM)
          case code =>
            error(
              s"Error occurred while launching fluentd (error code:$code). If you see 'LoadError', install fluentd and its dependencies by 'gem install fluentd'")
        }
      }
    })
    t.setDaemon(true)
    t.start()

    config.port
  }

  /**
    * Start fluentd and await its startup
    * @return fluentd port number
    */
  def startAndAwait: Int = {
    val port = start

    // Wait until fluentd starts
    var waitDuration = config.initialWaitMilliSeconds
    var count        = 0
    var connected    = false
    while (!connected && waitDuration < config.maxWaitMilliSeconds) {
      Thread.sleep(waitDuration)
      try {
        val sock = new Socket("localhost", port)
        sock.close()
        connected = true
      } catch {
        case e: IOException =>
          warn(s"${e.getMessage}")
          waitDuration = (waitDuration * 1.5).toInt
          count += 1
      }
    }

    if (!connected)
      throw new IOException("Failed to connect fluentd")

    port
  }

  def stop {
    fluentdProcess.map { p =>
      info(s"Terminating fluentd")
      p.destroy()
    }
  }

  private[fluentd] def prepare(config: FluentdConfig) = {

    def mkdir(path: File) {
      path.mkdirs()
      if (!path.exists())
        throw new IOException(s"Failed to create directory: ${path}")
    }

    // Create workdir

    val workDir   = new File(config.workDir)
    val coreDir   = new File(workDir, "core")
    val pluginDir = new File(workDir, "plugin")
    debug(s"Creating fluentd workdir: $workDir")
    mkdir(workDir)
    mkdir(coreDir)
    mkdir(pluginDir)

    // Copy fluentd
    for (f <- Resource.listResources("xerial.fluentd.core")) {

      def relPath(f: VirtualFile) = f.logicalPath.replaceFirst("xerial/fluent/core/", "")
      val rpath                   = relPath(f)
      val targetPath              = new File(coreDir, rpath)

      if (f.isDirectory)
        targetPath.mkdirs()
      else {
        IOUtil.withResource(f.url.openStream) { r =>
          IOUtil.readFully(r) { fileContents =>
            targetPath.getParentFile().mkdirs()
            IOUtil.withResource(new FileOutputStream(targetPath)) { out =>
              out.write(fileContents)
            }

            if (rpath.startsWith("bin"))
              targetPath.setExecutable(true)
          }
        }
      }
    }

    def fluentConf(port: Int, configuration: String) =
      s"""
        |## Listen a socket
        |<source>
        |  type forward
        |  port ${port}
        |</source>
        |
        |${configuration}
      """.stripMargin

    // Create fluent.conf if it doesn't specified
    if (config.configFile == null) {
      val targetFile = config.getConfigFile
      // Create fluent.conf from a template
      val configText = fluentConf(config.port, config.configuration)
      // Write fluent.conf
      IOUtil.withResource(new FileWriter(targetFile)) { writer =>
        writer.write(configText)
      }
    }
  }

}
