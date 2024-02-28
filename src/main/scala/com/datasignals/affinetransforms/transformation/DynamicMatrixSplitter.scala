package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.{ArrayIndex, OrderedChoice}
import com.datasignals.affinetransforms.transformation.DynamicMatrixMixer.MaxParallelism

import java.lang.Long.{BYTES => LONG_BYTES}
import java.util.{NavigableMap, TreeMap}

object DynamicMatrixSplitter {

  private val thresholds = Array(98304.0f)

  def apply(dimension: Int, matrix: Array[Long]): Splitter[ArrayIndex[Byte]] = {
    val map = new TreeMap[Float, Splitter[ArrayIndex[Byte]]]()
    map.put(thresholds(0), new ConcurrentNativeMatrixSplitter(dimension, matrix, MaxParallelism))
    new DynamicMatrixSplitter(map, new NativeMatrixSplitter(dimension, matrix))
  }
}

class DynamicMatrixSplitter(map: NavigableMap[Float, Splitter[ArrayIndex[Byte]]], default: Splitter[ArrayIndex[Byte]])
  extends Splitter[ArrayIndex[Byte]]{

  private[this] val choice = new OrderedChoice[Float, Splitter[ArrayIndex[Byte]]](map, default)

  override val dimension: Int = default.dimension

  private[this] val DLong: Float = dimension.toFloat / LONG_BYTES.toFloat

  @inline private def value(in: ArrayIndex[Byte]): Float = DLong * in.length.toFloat

  override def apply(out: Array[ArrayIndex[Byte]], in: ArrayIndex[Byte]): Unit = choice(value(in))(out, in)
}
