//--------------------------------------
//
// FluentdStandaloneTest.scala
// Since: 2013/12/16 10:50 AM
//
//--------------------------------------

package xerial.fluentd

import xerial.fluentd.MySpec

/**
 * @author Taro L. Saito
 */
class FluentdStandaloneTest extends MySpec {

  "FluentdStandaloe" should {

    "launch fluentd" in {

      FluentdStandalone.start();

    }


  }

}