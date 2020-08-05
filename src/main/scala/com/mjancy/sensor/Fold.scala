package com.mjancy.sensor

trait Fold[Acc, E] {
  val empty: Acc
  def combine(acc: Acc, e: E): Acc
}
