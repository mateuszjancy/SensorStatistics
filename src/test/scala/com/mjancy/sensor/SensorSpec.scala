package com.mjancy.sensor

import cats.implicits._
import org.scalatest.EitherValues

import scala.util.Try

class SensorSpec extends UnitTest with EitherValues {
  "Sensor" should "create case class from csv line with valid humidity" in {
    //given
    val testData = "s2,80"

    //when then
    Sensor(testData) shouldBe Sensor("s2", 80.toShort.asRight)
  }

  it should "create case class from csv line with NaN humidity" in {
    //given
    val testData = "s2,NaN"

    //when
    val result = Sensor(testData)

    //then
    assert(result.humidity.left.value.getMessage == "For input string: \"NaN\"")
    assert(result.sensorId == "s2")
  }
}
