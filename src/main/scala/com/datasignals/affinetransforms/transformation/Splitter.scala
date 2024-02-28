package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.HasDimension

trait Splitter[T] extends ((Array[T], T) => Unit) with HasDimension
