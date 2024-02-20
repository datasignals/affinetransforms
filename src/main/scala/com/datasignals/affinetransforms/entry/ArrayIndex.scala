package com.datasignals.affinetransforms.entry

object ArrayIndex {
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Short): Unit = Bits.putBytes(index.array, index.pos + pos, x)
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Int): Unit = Bits.putBytes(index.array, index.pos + pos, x)
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Long): Unit = Bits.putBytes(index.array, index.pos + pos, x)
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Float): Unit = Bits.putBytes(index.array, index.pos + pos, x)
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Double): Unit = Bits.putBytes(index.array, index.pos + pos, x)
  @inline final def putBytes(index: ArrayIndex[Byte], pos: Int, x: Char): Unit = Bits.putBytes(index.array, index.pos + pos, x)

  @inline final def getShort(index: ArrayIndex[Byte], pos: Int): Short = Bits.getShortUnsafe(index.array, index.pos + pos)
  @inline final def getInt(index: ArrayIndex[Byte], pos: Int): Int = Bits.getIntUnsafe(index.array, index.pos + pos)
  @inline final def getLong(index: ArrayIndex[Byte], pos: Int): Long = Bits.getLongUnsafe(index.array, index.pos + pos)
  @inline final def getFloat(index: ArrayIndex[Byte], pos: Int): Float = Bits.getFloatUnsafe(index.array, index.pos + pos)
  @inline final def getDouble(index: ArrayIndex[Byte], pos: Int): Double = Bits.getDoubleUnsafe(index.array, index.pos + pos)
  @inline final def getChar(index: ArrayIndex[Byte], pos: Int): Char = Bits.getCharUnsafe(index.array, index.pos + pos)
}

final class ArrayIndex[T](val array: Array[T], private var pos: Int, private val _length: Int) extends Index[T] {
  @inline def this(array: Array[T], pos: Int) = this(array, pos, array.length)

  @inline def this(arrayIndex: ArrayIndex[T], length: Int) = this(arrayIndex.array, arrayIndex.pos, length)
  @inline def this(arrayIndex: ArrayIndex[T]) = this(arrayIndex.array, arrayIndex.pos, arrayIndex._length)

  @inline override def length: Int = _length - pos
  @inline override def position: Int = pos

  @inline override def apply(i: Int): T = array(pos + i)
  @inline override def update(i: Int, e: T): Unit = { array(pos + i) = e }

  @inline def update(i: Int, index: ArrayIndex[T]): Unit =
    System.arraycopy(index.array, index.pos, array, pos + i, index.length)

  @inline override def +=(i: Int): Unit = { pos += i }
  @inline override def -=(i: Int): Unit = { pos -= i }
}
