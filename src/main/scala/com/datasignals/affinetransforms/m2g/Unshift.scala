package com.datasignals.affinetransforms.m2g

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.transformation.BlockTransformation

import java.lang.Long.{BYTES => LONG_BYTES}

class Unshift(private[this] val shift: Array[Long],
              private[this] val nonceSubblocks: Int = 0) extends BlockTransformation {

  private[this] val nonceSize: Int = nonceSubblocks << LOG_LONG_BYTES
  private[this] val outSubblocks: Int = shift.length
  private[this] val inSubblocks: Int = outSubblocks + nonceSubblocks

  override val outBlockSize: Int = outSubblocks << LOG_LONG_BYTES
  override val inBlockSize: Int = inSubblocks << LOG_LONG_BYTES

  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    var outOff = outOffset
    var bytes = nonceSize
    for(k <- 0 until outSubblocks) {
      if(bytes >= l) return bytes
      Bits.putBytes(out, outOff, Bits.getLongUnsafe(in, inOffset + bytes) - shift(k))
      outOff += LONG_BYTES
      bytes += LONG_BYTES
    }
    bytes
  }
}
