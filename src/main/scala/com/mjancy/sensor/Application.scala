package com.mjancy.sensor

import java.io.File
import java.nio.file.Path

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Framing, Source}
import akka.util.ByteString

import scala.concurrent.Future

trait Application {
  def pathParam(args: Array[String]): Either[String, String] =
    for {
      path <- Either.cond(args.length >= 1, args.head, "Path to sensor data location need to be passed.")
      _    <- Either.cond(path.nonEmpty, path, "Path cannot be empty.")
      _    <- Either.cond(new File(path).exists(), path, s"DIR:$path not exists.")
    } yield path

  def workDir(path: String): Either[String, Array[Path]] = {
    val files = new File(path)
      .listFiles()
      .filter(_.isFile)
      .filter(_.getName.toLowerCase().endsWith(".csv"))
      .map(_.toPath)

    Either.cond(files.nonEmpty, files, s"No *.csv files found in given [path:$path]")
  }

  def pathSource(path: Array[Path]): Source[Path, NotUsed] = Source(path)

  def fileStore(path: Path): Source[ByteString, Future[IOResult]] =
    FileIO
      .fromPath(path)
      .via(Framing.delimiter(ByteString("\n"), 100))
      .drop(Header)

  def progressBar(log: LoggingAdapter): Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].zipWithIndex
      .map {
        case (data, idx) =>
          if (idx % 15000000 == 0) {
            val mb      = 1024 * 1024
            val runtime = Runtime.getRuntime
            log.info(s"Picked:$idx rows.")
            log.info(s"** Used Memory:  ${(runtime.totalMemory - runtime.freeMemory) / mb} MB")
            log.info(s"** Free Memory:  ${runtime.freeMemory / mb} MB")
            log.info(s"** Total Memory: ${runtime.totalMemory / mb} MB")
            log.info(s"** Max Memory:   ${runtime.maxMemory / mb} MB")
          }
          data
      }

  def calculationFlow: Flow[ByteString, Map[String, Calculation], NotUsed] =
    Flow[ByteString]
      .map(_.utf8String)
      .map(Sensor.apply)
      .fold(Calculation.empty)(Calculation.combine)

  def report(processedFiles: Array[Path]): Flow[Map[String, Calculation], String, NotUsed] = Flow[Map[String, Calculation]].map { report =>
    val processed    = processedFiles.length
    val measurements = report.values.map(_.count).sum
    val failed       = report.values.map(_.errors).sum
    val data = report
      .toList
      .map {
        case (sensorId, calculation) =>
          calculation.stats.fold(
            _ => BigInt(-1) -> s"$sensorId,NaN,NaN,NaN",
            stats => {
              val avg: BigInt = stats.sum / calculation.count
              avg -> s"$sensorId,${stats.min},$avg,${stats.max}"
            }
          )
      }
      .sortBy { case (avg, _) => avg * -1}
      .map { case (_, data) => data }

    s"""
       |Num of processed files: $processed
       |Num of processed measurements: ${measurements + failed}
       |Num of failed measurements: $failed
       |
       |Sensors with highest avg humidity:
       |
       |sensor-id,min,avg,max
       |${data.mkString("\n")}
       |""".stripMargin
  }

  private val Header = 1
}
