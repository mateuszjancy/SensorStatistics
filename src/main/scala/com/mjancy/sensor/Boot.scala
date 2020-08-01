package com.mjancy.sensor

import akka.actor.ActorSystem
import cats.implicits._

import scala.concurrent._

object Boot extends App with Application {
  implicit val system: ActorSystem          = ActorSystem("SensorStatistics")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  system.log.info("""
      |
      |   _____                           _____ _        _   _     _   _
      |  / ____|                         / ____| |      | | (_)   | | (_)
      | | (___   ___ _ __  ___  ___  _ _| (___ | |_ __ _| |_ _ ___| |_ _  ___ ___
      |  \___ \ / _ \ '_ \/ __|/ _ \| '__\___ \| __/ _` | __| / __| __| |/ __/ __|
      |  ____) |  __/ | | \__ \ (_) | |  ____) | || (_| | |_| \__ \ |_| | (__\__ \
      | |_____/ \___|_| |_|___/\___/|_| |_____/ \__\__,_|\__|_|___/\__|_|\___|___/
      |
      | By Mateusz
      |""".stripMargin)

  def run(params: Array[String])(implicit ec: ExecutionContext, system: ActorSystem): Future[String] = {
    val MaxConcurrentFiles = 5

    val result: Either[String, Future[String]] = for {
      path   <- pathParam(params)
      dir    <- workDir(path)
      result <- pathSource(dir).flatMapMerge(MaxConcurrentFiles, fileStore).via(progressBar(system.log)).via(calculationFlow).via(report(dir)).asRight
    } yield result.runReduce(_ + _)

    result.fold(error => { system.log.error(error); system.terminate(); Future.successful(error) }, f => { f.onComplete(_ => system.terminate()); f })
  }

  run(args).map(system.log.info)
}
