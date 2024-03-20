package com.datasignals.affinetransforms.entry

import java.util.NavigableMap

final class OrderedChoice[-T, +U](private[this] val thresholdMap: NavigableMap[T, U], private[this] val minValue: U)
  extends Choice[T, U] {

  @inline override def apply(key: T): U = {
    val entry = thresholdMap.floorEntry(key)
    if(entry == null) return minValue
    entry.getValue
  }
}
