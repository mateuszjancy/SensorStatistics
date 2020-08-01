package com.mjancy.sensor

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait UnitTest extends AnyFlatSpec with Matchers {
  val NaN = new NumberFormatException("For input string: \"NaN\"")
}
