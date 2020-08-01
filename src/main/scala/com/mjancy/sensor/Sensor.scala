package com.mjancy.sensor

import com.mjancy.sensor
import com.mjancy.sensor.Sensor.Humidity

import scala.util.Try

object Sensor {
  type Humidity = Either[Throwable, Short]

  private val Sep = ","

  def apply(row: String): Sensor = {
    row.split(Sep) match {
      case Array(id, humidity, _*) => sensor.Sensor(id, Try(humidity.toShort).toEither)
      case invalidRow              => throw new IllegalStateException(s"Invalid row:${invalidRow.mkString(",")}")
    }
  }
}

final case class Sensor(sensorId: String, humidity: Humidity)
