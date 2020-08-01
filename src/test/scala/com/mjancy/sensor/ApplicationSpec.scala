package com.mjancy.sensor

import java.io.File

import org.scalatest.EitherValues

class ApplicationSpec extends UnitTest with EitherValues with Application {
  val Path: String = new File(".").getAbsolutePath

  "path" should "fail for empty args" in {
    pathParam(Array.empty[String]).left.value shouldBe "Path to sensor data location need to be passed."
  }

  it should "fail for empty path" in {
    pathParam(Array("")).left.value shouldBe "Path cannot be empty."
  }

  it should "fail for not existing path" in {
    val path = Path + "/not/existing/path"
    pathParam(Array(path)).left.value shouldBe s"DIR:$path not exists."
  }

  it should "fail for existing path" in {
    val result = pathParam(Array(Path))
    assert(result.isRight)
    result.map(_ shouldBe Path)
  }
}
