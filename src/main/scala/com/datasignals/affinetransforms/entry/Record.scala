package com.datasignals.affinetransforms.entry

trait Record[+K, +V] extends HasKey[K] with HasValue[V] {
  override def equals(obj: Any): Boolean = {
    obj match {
      case r: Record[_, _] => key == r.key && value == r.value
      case _ => false
    }
  }

  override def toString: String = {
    s"Record($key, $value)"
  }
}
