package com.datasignals.affinetransforms.entry

trait Index[T] {
  def length: Int
  def position: Int

  def apply(i: Int): T
  def update(i: Int, e: T): Unit

  def +=(i: Int): Unit
  def -=(i: Int): Unit
}
