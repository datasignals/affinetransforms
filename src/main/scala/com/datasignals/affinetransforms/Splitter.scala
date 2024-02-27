package com.datasignals.affinetransforms

trait Splitter[T] extends ((Array[T], T) => Unit) with HasDimension
