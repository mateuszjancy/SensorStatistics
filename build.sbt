name := "SensorStatistics"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.8",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.2.0" % "test"
)
