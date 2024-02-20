package com.datasignals.affinetransforms.entry

import jdk.internal.misc.Unsafe

import java.lang.Character.{BYTES => CHAR_BYTES}
import java.lang.Double.{BYTES => DOUBLE_BYTES}
import java.lang.Float.{BYTES => FLOAT_BYTES}
import java.lang.Integer.{BYTES => INT_BYTES}
import java.lang.Long.{BYTES => LONG_BYTES}
import java.lang.Short.{BYTES => SHORT_BYTES}
import java.lang.{Double => JavaDouble, Float => JavaFloat}

object Bits {

  final private val UNSAFE: Unsafe = Unsafe.getUnsafe
  final private val ARRAY_BASE_OFFSET: Long = UNSAFE.arrayBaseOffset(classOf[Array[Byte]])

  final val LOG_BYTE_BYTES: Int = 0
  final val LOG_SHORT_BYTES: Int = 1
  final val LOG_INT_BYTES: Int = 2
  final val LOG_LONG_BYTES: Int = 3

  final val LOG_CHAR_BYTES: Int = 1

  final val LOG_FLOAT_BYTES: Int = 2
  final val LOG_DOUBLE_BYTES: Int = 3

  object Length {

    final private val SHORT_REMAINDER_MASK = SHORT_BYTES - 1
    final private val INT_REMAINDER_MASK = INT_BYTES - 1
    final private val LONG_REMAINDER_MASK = LONG_BYTES - 1

    final private val CHAR_REMAINDER_MASK = CHAR_BYTES - 1
    final private val FLOAT_REMAINDER_MASK = FLOAT_BYTES - 1
    final private val DOUBLE_REMAINDER_MASK = DOUBLE_BYTES - 1

    final def shortLength(length: Int): Int = {
      val r: Int = length & SHORT_REMAINDER_MASK
      val k: Int = length >> LOG_SHORT_BYTES
      if(r > 0) k + 1 else k
    }

    def intLength(length: Int): Int = {
      val r: Int = length & INT_REMAINDER_MASK
      val k: Int = length >> LOG_INT_BYTES
      if(r > 0) k + 1 else k
    }

    def longLength(length: Int): Int = {
      val r: Int = length & LONG_REMAINDER_MASK
      val k: Int = length >> LOG_LONG_BYTES
      if(r > 0) k + 1 else k
    }

    def charLength(length: Int): Int = {
      val r: Int = length & CHAR_REMAINDER_MASK
      val k: Int = length >> LOG_CHAR_BYTES
      if(r > 0) k + 1 else k
    }

    def floatLength(length: Int): Int = {
      val r: Int = length & FLOAT_REMAINDER_MASK
      val k: Int = length >> LOG_FLOAT_BYTES
      if(r > 0) k + 1 else k
    }

    def doubleLength(length: Int): Int = {
      val r: Int = length & DOUBLE_REMAINDER_MASK
      val k: Int = length >> LOG_DOUBLE_BYTES
      if(r > 0) k + 1 else k
    }
  }

  @inline final private def makeLong8(b7: Byte, b6: Byte, b5: Byte, b4: Byte, b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b7.toLong << 56) |
      ((b6.toLong & 0xff) << 48) |
      ((b5.toLong & 0xff) << 40) |
      ((b4.toLong & 0xff) << 32) |
      ((b3.toLong & 0xff) << 24) |
      ((b2.toLong & 0xff) << 16) |
      ((b1.toLong & 0xff) << 8) |
      (b0.toLong & 0xff)

  @inline final private def makeLong7(b6: Byte, b5: Byte, b4: Byte, b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b6.toLong << 56) |
      ((b5.toLong & 0xff) << 48) |
      ((b4.toLong & 0xff) << 40) |
      ((b3.toLong & 0xff) << 32) |
      ((b2.toLong & 0xff) << 24) |
      ((b1.toLong & 0xff) << 16) |
      ((b0.toLong & 0xff) << 8)

  @inline final private def makeLong6(b5: Byte, b4: Byte, b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b5.toLong << 56) |
      ((b4.toLong & 0xff) << 48) |
      ((b3.toLong & 0xff) << 40) |
      ((b2.toLong & 0xff) << 32) |
      ((b1.toLong & 0xff) << 24) |
      ((b0.toLong & 0xff) << 16)

  @inline final private def makeLong5(b4: Byte, b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b4.toLong << 56) |
      ((b3.toLong & 0xff) << 48) |
      ((b2.toLong & 0xff) << 40) |
      ((b1.toLong & 0xff) << 32) |
      ((b0.toLong & 0xff) << 24)

  @inline final private def makeLong4(b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b3.toLong << 56) |
      ((b2.toLong & 0xff) << 48) |
      ((b1.toLong & 0xff) << 40) |
      ((b0.toLong & 0xff) << 32)

  @inline final private def makeLong3(b2: Byte, b1: Byte, b0: Byte) =
    (b2.toLong << 56) |
      ((b1.toLong & 0xff) << 48) |
      ((b0.toLong & 0xff) << 40)

  @inline final private def makeLong2(b1: Byte, b0: Byte) =
    (b1.toLong << 56) |
      ((b0.toLong & 0xff) << 48)

  final def getLong(bytes: Array[Byte], pos: Int): Long = {
    bytes.length - pos match {
      case 0 => 0L
      case 1 => bytes (pos).toLong << 56
      case 2 => makeLong2 (bytes (pos), bytes (pos + 1) )
      case 3 => makeLong3 (bytes (pos), bytes (pos + 1), bytes (pos + 2) )
      case 4 => makeLong4 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3) )
      case 5 => makeLong5 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3), bytes (pos + 4) )
      case 6 => makeLong6 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3), bytes (pos + 4), bytes (pos + 5) )
      case 7 => makeLong7 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3), bytes (pos + 4), bytes (pos + 5), bytes (pos + 6) )
      case _ => makeLong8 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3), bytes (pos + 4), bytes (pos + 5), bytes (pos + 6), bytes (pos + 7) )
    }
  }

  @inline final def getLongUnsafe(bytes: Array[Byte], pos: Int): Long = {
    bytes.length - pos match {
      case 0 => 0L
      case 1 => bytes(pos).toLong << 56
      case 2 => UNSAFE.getShortUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true).toLong << 48
      case 3 => makeLong3(bytes(pos), bytes(pos + 1), bytes(pos + 2))
      case 4 => UNSAFE.getIntUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true).toLong << 32
      case 5 => makeLong5(bytes(pos), bytes(pos + 1), bytes(pos + 2), bytes(pos + 3), bytes(pos + 4))
      case 6 => makeLong6(bytes(pos), bytes(pos + 1), bytes(pos + 2), bytes(pos + 3), bytes(pos + 4), bytes(pos + 5))
      case 7 => makeLong7(bytes(pos), bytes(pos + 1), bytes(pos + 2), bytes(pos + 3), bytes(pos + 4), bytes(pos + 5), bytes(pos + 6))
      case _ => UNSAFE.getLongUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true)
    }
  }

  @inline final private def makeInt4(b3: Byte, b2: Byte, b1: Byte, b0: Byte) =
    (b3.toInt << 24) |
      ((b2.toInt & 0xff) << 16) |
      ((b1.toInt & 0xff) << 8) |
      (b0.toInt & 0xff)

  @inline final private def makeInt3(b2: Byte, b1: Byte, b0: Byte) =
    (b2.toInt << 24) |
      ((b1.toInt & 0xff) << 16) |
      ((b0.toInt & 0xff) << 8)

  @inline final private def makeInt2(b1: Byte, b0: Byte) =
    (b1.toInt << 24) |
      ((b0.toInt & 0xff) << 16)

  final def getInt(bytes: Array[Byte], pos: Int): Int = {
    bytes.length - pos match {
      case 0 => 0
      case 1 => bytes (pos).toInt << 24
      case 2 => makeInt2 (bytes (pos), bytes (pos + 1) )
      case 3 => makeInt3 (bytes (pos), bytes (pos + 1), bytes (pos + 2) )
      case _ => makeInt4 (bytes (pos), bytes (pos + 1), bytes (pos + 2), bytes (pos + 3) )
    }
  }

  @inline final def getIntUnsafe(bytes: Array[Byte], pos: Int): Int = {
    bytes.length - pos match {
      case 0 => 0
      case 1 => bytes (pos).toInt << 24
      case 2 => UNSAFE.getShortUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true).toInt << 16
      case 3 => makeInt3 (bytes (pos), bytes (pos + 1), bytes (pos + 2) )
      case _ => UNSAFE.getIntUnaligned(bytes, ARRAY_BASE_OFFSET + pos,true)
    }
  }

  @inline final def getShortUnsafe(bytes: Array[Byte], pos: Int): Short = {
    bytes.length - pos match {
      case 0 => 0.toShort
      case 1 => (bytes (pos) << 8).toShort
      case _ => UNSAFE.getShortUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true)
    }
  }

  @inline final def getCharUnsafe(bytes: Array[Byte], pos: Int): Char = {
    bytes.length - pos match {
      case 0 => 0.toChar
      case 1 => (bytes (pos) << 8).toChar
      case _ => UNSAFE.getCharUnaligned(bytes, ARRAY_BASE_OFFSET + pos, true)
    }
  }

  @inline final def getFloatUnsafe(bytes: Array[Byte], pos: Int): Float = {
    JavaFloat.intBitsToFloat(getIntUnsafe(bytes, pos))
  }

  @inline final def getDoubleUnsafe(bytes: Array[Byte], pos: Int): Double = {
    JavaDouble.longBitsToDouble(getLongUnsafe(bytes, pos))
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getShorts(out: Array[Short], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getShortUnsafe(in, i)
      j += 1
      i += SHORT_BYTES
    }
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getInts(out: Array[Int], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getIntUnsafe(in, i)
      j += 1
      i += INT_BYTES
    }
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getLongs(out: Array[Long], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getLongUnsafe(in, i)
      j += 1
      i += LONG_BYTES
    }
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getChars(out: Array[Char], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getCharUnsafe(in, i)
      j += 1
      i += CHAR_BYTES
    }
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getFloats(out: Array[Float], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getFloatUnsafe(in, i)
      j += 1
      i += FLOAT_BYTES
    }
  }

  //noinspection AccessorLikeMethodIsUnit
  final def getDoubles(out: Array[Double], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Unit = {
    var j = outOffset
    var i = inOffset
    val inEnd = inOffset + length
    while (i < inEnd) {
      out(j) = getDoubleUnsafe(in, i)
      j += 1
      i += DOUBLE_BYTES
    }
  }

  final def putLongs(longs: Array[Long], lPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = lPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      longs(j) = getLongUnsafe(bytes, i)
      j += 1
      i += LONG_BYTES
    }
  }

  final def putInts(ints: Array[Int], iPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = iPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      ints(j) = getIntUnsafe(bytes, i)
      j += 1
      i += INT_BYTES
    }
  }

  final def putShorts(shorts: Array[Short], iPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = iPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      shorts(j) = getShortUnsafe(bytes, i)
      j += 1
      i += SHORT_BYTES
    }
  }

  final def putFloats(floats: Array[Float], iPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = iPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      floats(j) = getFloatUnsafe(bytes, i)
      j += 1
      i += FLOAT_BYTES
    }
  }

  final def putDoubles(doubles: Array[Double], iPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = iPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      doubles(j) = getDoubleUnsafe(bytes, i)
      j += 1
      i += DOUBLE_BYTES
    }
  }

  final def putChars(chars: Array[Char], iPos: Int, bytes: Array[Byte], pos: Int): Unit = {
    var j: Int = iPos
    var i = pos
    val length = bytes.length
    while (i < length) {
      chars(j) = getCharUnsafe(bytes, i)
      j += 1
      i += CHAR_BYTES
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Long): Unit =
    UNSAFE.putLongUnaligned(bytes, ARRAY_BASE_OFFSET + pos, x, true)

  final def putLongBytes(bytes: Array[Byte], pos: Int, longs: Array[Long], lPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET  + pos
    var i = lPos
    val length = longs.length
    while(i < length) {
      UNSAFE.putLongUnaligned(bytes, j, longs(i), true)
      j += LONG_BYTES
      i += 1
    }
  }

  final def putLongBytes(bytes: Array[Byte], pos: Int, longs: Iterable[Long]): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    for(l <- longs) {
      UNSAFE.putLongUnaligned(bytes, j, l, true)
      j += LONG_BYTES
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Int): Unit =
    UNSAFE.putIntUnaligned(bytes, ARRAY_BASE_OFFSET + pos, x, true)

  final def putIntBytes(bytes: Array[Byte], pos: Int, ints: Array[Int], iPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    var i = iPos
    val length = ints.length
    while(i < length) {
      UNSAFE.putIntUnaligned(bytes, j, ints(i), true)
      j += INT_BYTES
      i += 1
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Short): Unit =
    UNSAFE.putShortUnaligned(bytes, ARRAY_BASE_OFFSET + pos, x, true)

  final def putShortBytes(bytes: Array[Byte], pos: Int, shorts: Array[Short], iPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    var i = iPos
    val length = shorts.length
    while(i < length) {
      UNSAFE.putShortUnaligned(bytes, j, shorts(i), true)
      j += SHORT_BYTES
      i += 1
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Float): Unit =
    UNSAFE.putIntUnaligned(bytes, ARRAY_BASE_OFFSET + pos, JavaFloat.floatToRawIntBits(x), true)

  final def putFloatBytes(bytes: Array[Byte], pos: Int, floats: Array[Float], iPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    var i = iPos
    val length = floats.length
    while(i < length) {
      UNSAFE.putIntUnaligned(bytes, j, JavaFloat.floatToRawIntBits(floats(i)), true)
      j += FLOAT_BYTES
      i += 1
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Double): Unit =
    UNSAFE.putLongUnaligned(bytes, ARRAY_BASE_OFFSET + pos, JavaDouble.doubleToRawLongBits(x), true)

  final def putDoubleBytes(bytes: Array[Byte], pos: Int, doubles: Array[Double], iPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    var i = iPos
    val length = doubles.length
    while(i < length) {
      UNSAFE.putLongUnaligned(bytes, j, JavaDouble.doubleToRawLongBits(doubles(i)), true)
      j += DOUBLE_BYTES
      i += 1
    }
  }

  @inline final def putBytes(bytes: Array[Byte], pos: Int, x: Char): Unit =
    UNSAFE.putCharUnaligned(bytes, ARRAY_BASE_OFFSET + pos, x, true)

  final def putCharBytes(bytes: Array[Byte], pos: Int, chars: Array[Char], iPos: Int = 0): Unit = {
    var j: Long = ARRAY_BASE_OFFSET + pos
    var i = iPos
    val length = chars.length
    while(i < length) {
      UNSAFE.putCharUnaligned(bytes, j,chars(i), true)
      j += CHAR_BYTES
      i += 1
    }
  }

}
