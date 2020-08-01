package com.mjancy.sensor

import akka.actor.ActorSystem
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContextExecutor

class BootItSpec extends AsyncFlatSpec with Matchers {
  val path: String                          = getClass.getResource("/it-test").getPath
  implicit val system: ActorSystem          = ActorSystem("SensorStatistics")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  "Boot.run" should "build report" in {
    val expected =
      """
        |Num of processed files: 2
        |Num of processed measurements: 7
        |Num of failed measurements: 2
        |
        |Sensors with highest avg humidity:
        |
        |sensor-id,min,avg,max
        |s2,78,82,88
        |s1,10,54,98
        |s3,NaN,NaN,NaN
        |""".stripMargin

    Boot.run(Array(path)).map(_ shouldBe expected)
  }

}
