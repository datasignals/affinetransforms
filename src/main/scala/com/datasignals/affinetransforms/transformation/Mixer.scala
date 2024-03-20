package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.HasDimension

trait Mixer[T] extends ((T, Array[T]) => Unit) with HasDimension
