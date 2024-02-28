package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.{ArrayIndex, OrderedChoice}

import java.lang.Long.{BYTES => LONG_BYTES}
import java.util.{NavigableMap, TreeMap}

object DynamicMatrixMixer {

  private val thresholds = Array(98304.0f)
  final val MaxParallelism: Int = Runtime.getRuntime.availableProcessors()

  def apply(dimension: Int, matrix: Array[Long]): Mixer[ArrayIndex[Byte]] = {
    val map = new TreeMap[Float, Mixer[ArrayIndex[Byte]]]()
    map.put(thresholds(0), new ConcurrentNativeMatrixMixer(dimension, matrix, MaxParallelism))
    new DynamicMatrixMixer(map, new NativeMatrixMixer(dimension, matrix))
  }
}

class DynamicMatrixMixer(map: NavigableMap[Float, Mixer[ArrayIndex[Byte]]], default: Mixer[ArrayIndex[Byte]])
  extends Mixer[ArrayIndex[Byte]]{

  private[this] val choice = new OrderedChoice[Float, Mixer[ArrayIndex[Byte]]](map, default)

  override val dimension: Int = default.dimension

  private[this] val DxDLong: Float = {
    val d = dimension.toFloat
    (d / LONG_BYTES.toFloat) * d
  }

  @inline private def value(in: Array[ArrayIndex[Byte]]): Float = DxDLong * in.head.length.toFloat

  override def apply(out: ArrayIndex[Byte], in: Array[ArrayIndex[Byte]]): Unit = choice(value(in))(out, in)
}
