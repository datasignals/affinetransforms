package com.datasignals.affinetransforms

trait Mixer[T] extends ((T, Array[T]) => Unit) with HasDimension
