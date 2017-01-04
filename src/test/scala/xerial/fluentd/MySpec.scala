package xerial.fluentd

import org.scalatest._
import xerial.core.io.Resource
import xerial.core.util.Timer
import wvlet.log.Logger
import wvlet.log.LogSupport
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import scala.language.implicitConversions

//--------------------------------------
//
// MySpec.scala
// Since: 2012/11/20 12:57 PM
//
//--------------------------------------

/**
 * Helper trait for writing test codes. Extend this trait in your test classes
 */
trait MySpec extends WordSpec with Matchers with GivenWhenThen with OptionValues with Resource with Timer with LogSupport with BeforeAndAfter {

  Logger.setDefaultFormatter(SourceCodeLogFormatter)

  implicit def toTag(s:String) = Tag(s)

}
