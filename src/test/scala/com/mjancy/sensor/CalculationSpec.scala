package com.mjancy.sensor

import cats.implicits._

class CalculationSpec extends UnitTest {
  "Calculation" should "provide statistics" in {
    //given
    val sensors = List(
      Sensor(sensorId = "s2", humidity = 80.toShort.asRight),
      Sensor(sensorId = "s3", humidity = NaN.asLeft),
      Sensor(sensorId = "s2", humidity = 78.toShort.asRight),
      Sensor(sensorId = "s1", humidity = 98.toShort.asRight),
      Sensor(sensorId = "s1", humidity = 10.toShort.asRight),
      Sensor(sensorId = "s2", humidity = 88.toShort.asRight),
      Sensor(sensorId = "s1", humidity = NaN.asLeft)
    )

    val expected = Map(
      "s1" -> Calculation(Stats(min = 10, sum = 108, max = 98).asRight, count = 2, errors = 1),
      "s2" -> Calculation(Stats(min = 78, sum = 246, max = 88).asRight, count = 3, errors = 0),
      "s3" -> Calculation(NaN.asLeft, count = 0, errors = 1)
    )

    //when
    val result = sensors.foldLeft(Calculation.empty)(Calculation.combine)

    //then
    result should contain theSameElementsAs expected
  }
}
