package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.transformation.BlockTransformation
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}


class DecryptAndUnshift(private[this] val cipher: () => BlockCipher,
                        private[this] val params: KeyParameter)
                       (private[this] val shift: Array[Long]) extends BlockTransformation {

  private[this] val cipherBlockSize: Int = cipher().getBlockSize
  private[this] val cipherSubblocks: Int = cipherBlockSize >> LOG_LONG_BYTES
  println(s"cipherSubblocks $cipherSubblocks\n")
  private[this] val nonceSize: Int = cipherBlockSize - LONG_BYTES
  private[this] val outSubblocks: Int = shift.length
  println(s"outSubblocks $outSubblocks\n")
  assume(cipherBlockSize % LONG_BYTES == 0)

  override val outBlockSize: Int = outSubblocks << LOG_LONG_BYTES
  override val inBlockSize: Int = outBlockSize + nonceSize
  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    if(l <= nonceSize) return l

    val c = cipher()
    c.init(true, params)
    val counter = new Array[Byte](cipherBlockSize)
    System.arraycopy(in, inOffset, counter, 0, nonceSize)
    val counterOut = new Array[Byte](cipherBlockSize)

    val ll = l - nonceSize
    var subblocks = (ll / cipherBlockSize) + (if(ll % cipherBlockSize == 0) 0 else 1)

    var inOff: Int = inOffset + nonceSize
    var outOff = outOffset
    var bytes = nonceSize
    var shiftCnt = 0
    subblocks=1 //HARD CODED!!!!!!!!!!!!!!!!!
    for(k <- 0 until subblocks) {
      Bits.putBytes(counter, nonceSize, k)
      c.reset()
      c.processBlock(counter, 0, counterOut, 0)

      for(i <- 0 until cipherSubblocks) {
        if(bytes >= l) return bytes
        val a = Bits.getLongUnsafe(in, inOff)
        val c = Bits.getLongUnsafe(counterOut, i << LOG_LONG_BYTES)
        Bits.putBytes(out, outOff, (a - shift(shiftCnt)) ^ c)
        shiftCnt += 1
        outOff += LONG_BYTES
        inOff += LONG_BYTES
        bytes += LONG_BYTES
      }

    }
    bytes
  }
}
