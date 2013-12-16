//--------------------------------------
//
// FluentdStandaloneTest.scala
// Since: 2013/12/16 10:50 AM
//
//--------------------------------------

package xerial.fluentd


/**
 * @author Taro L. Saito
 */
class FluentdStandaloneTest extends MySpec {

  "FluentdStandaloe" should {

    "launch fluentd" in {

      val fluentd = FluentdStandalone.start()


      Thread.sleep(5000)
      fluentd.stop
    }


  }

}