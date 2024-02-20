package com.datasignals.affinetransforms.entry

import java.util.Arrays

class GenericRecord[+K, +V](val key: K, val value: V) extends Record[K, V] {
  override lazy val toString: String = s"GenericRecord($key, $value)"

  override def equals(obj: Any): Boolean = {
    obj match {
      case r: Record[K, V] =>
        (r.key match {
          case k: Array[Byte] =>
            key match {
              case k0: Array[Byte] => Arrays.equals(k0, k)
              case _ => false
            }
          case _ => key == r.key
        }) &&
          (r.value match {
            case v: Array[Byte] =>
              value match {
                case v0: Array[Byte] => Arrays.equals(v0, v)
                case _ => false
              }
            case _ => value == r.value
          })
      case _ => false
    }
  }
}
