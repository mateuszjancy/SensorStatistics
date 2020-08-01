package com.mjancy.sensor

import cats.implicits._
import com.mjancy.sensor.Sensor.Humidity

object Calculation {
  val empty = Map.empty[String, Calculation]

  def combine(acc: Map[String, Calculation], sensor: Sensor): Map[String, Calculation] = acc.updatedWith(sensor.sensorId) {
    case Some(sensorStatistics) =>
      val result = statistics(sensorStatistics, sensor.humidity)
      Some(result)
    case None =>
      val result = zero(sensor.humidity)
      Some(result)
  }

  private def statistics(sensorStatistics: Calculation, humidity: Humidity): Calculation = (sensorStatistics, humidity) match {
    case (Calculation(Right(Stats(min, sum, max)), count, _), Right(current)) =>
      val newMin   = if (current < min) current else min
      val newMax   = if (current > max) current else max
      val newCount = count + 1
      val newSum   = sum + current

      sensorStatistics.copy(
        stats = Stats(min = newMin, sum = newSum, max = newMax).asRight,
        count = newCount
      )
    case (Calculation(Left(_), _, _), Right(current)) =>
      sensorStatistics.copy(
        stats = Stats(current, current, current).asRight,
        count = 1
      )
    case (Calculation(_, _, errors), Left(_)) =>
      sensorStatistics.copy(
        errors = errors + 1
      )
  }

  private def zero(humidity: Humidity): Calculation = {
    val (count, errors) = if (humidity.isRight) (1, 0) else (0, 1)
    Calculation(humidity.map(initial => Stats(initial, initial, initial)), count, errors)
  }
}

final case class Stats(min: Int, sum: BigInt, max: Int)

final case class Calculation(stats: Either[Throwable, Stats], count: Int, errors: Int)
