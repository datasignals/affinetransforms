package com.datasignals.affinetransforms.m2g

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.transformation.BlockTransformation

import java.lang.Long.{BYTES => LONG_BYTES}

class Shift(private[this] val shift: Array[Long]) extends BlockTransformation {

  private[this] val inSubblocks: Int = shift.length

  override val inBlockSize: Int = inSubblocks << LOG_LONG_BYTES
  override val outBlockSize: Int = inBlockSize

  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    var outOff = outOffset
    var bytes = 0
    for(k <- 0 until inSubblocks) {
      if(bytes >= l) return bytes
      Bits.putBytes(out, outOff, Bits.getLongUnsafe(in, inOffset + bytes) + shift(k))
      outOff += LONG_BYTES
      bytes += LONG_BYTES
    }
    bytes
  }
}
