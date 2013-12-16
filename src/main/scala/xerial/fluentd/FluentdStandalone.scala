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
import xerial.fluentd.FluentdConfig

/**
 * @author Taro L. Saito
 */
object FluentdStandalone extends Logger {

  def apply : FluentdStandalone = {
    new FluentdStandalone(FluentdConfig())
  }


  def withFluentd(config:FluentdConfig) : {


  }


  private[fluentd] def createWorkDir(config:FluentdConfig) = {

    // Create workdir
    val workDir = new File(config.workDir)
    if(!workDir.mkdirs)
      throw new IOException(s"Failed to create working directory: ${workDir}}")

    // Create plugin folder
    if(!(workDir / "plugin").mkdirs)
      throw new IOException(s"Failed to create plugin directory: ${workDir / "plugin"}")

    // Create fluent.conf if it doesn't exist
    val targetFile = workDir / config.configFile
    if(!targetFile.exists()) {
      // Create fluent.conf from a template
      Resource.find("/xerial/fluent/fluentd.conf.template").map { templateURL =>
        val engine = new TemplateEngine
        val templateSource = engine.source(templateURL.toExternalForm, "mustache")
        val prop = Map[String, Any]("port" -> config.port)
        val configText = engine.layout(templateSource, prop)


        // Write fluent.conf
        IOUtil.withResource(new FileWriter(targetFile)) { writer =>
          writer.write(configText)
        }
      }
    }
  }


}

import FluentdStandalone._

/**
 * Fluentd configuration
 * @param port
 * @param configFile
 * @param workDir
 */
case class FluentdConfig(port:Int = IOUtil.randomPort,
                         configFile:String="fluent.conf",
                         workDir:String = "target/fluentd"
                          )


class FluentdStandalone(config:FluentdConfig) {

  createWorkDir(config)




}