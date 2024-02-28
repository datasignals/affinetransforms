package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.util.Random

object EncryptAndShift {

  @inline def cipherTextExpansion(cipher: BlockCipher): Int = {
    cipher.getBlockSize - LONG_BYTES
  }

}

class EncryptAndShift(private[this] val cipher: () => BlockCipher,
                      private[this] val params: KeyParameter,
                      private[this] val random: Random)
                     (private[this] val shift: Array[Long]) extends BlockTransformation {

  private[this] val cipherBlockSize: Int = cipher().getBlockSize
  private[this] val cipherSubblocks: Int = cipherBlockSize >> LOG_LONG_BYTES
  private[this] val nonceSize: Int = cipherBlockSize - LONG_BYTES
  private[this] val inSubblocks: Int = shift.length
  assume(cipherBlockSize % LONG_BYTES == 0)

  override val inBlockSize: Int = inSubblocks << LOG_LONG_BYTES
  override val outBlockSize: Int = inBlockSize + nonceSize

  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    if(l <= 0) return 0

    val iv = new Array[Byte](nonceSize)
    random.nextBytes(iv)
    System.arraycopy(iv, 0, out, outOffset, nonceSize)

    val c = cipher()
    c.init(true, params)
    val counter = new Array[Byte](cipherBlockSize)
    System.arraycopy(iv, 0, counter, 0, nonceSize)
    val counterOut = new Array[Byte](cipherBlockSize)

    val subblocks = (l / cipherBlockSize) + (if(l % cipherBlockSize == 0) 0 else 1)

    var outOff = outOffset + nonceSize
    var inOff = inOffset
    var bytes = 0
    var shiftCnt = 0
    for(k <- 0 until subblocks) {
      Bits.putBytes(counter, nonceSize, k)
      c.reset()
      c.processBlock(counter, 0, counterOut, 0)

      for(i <- 0 until cipherSubblocks) {
        if(bytes >= l) return bytes
        val a = Bits.getLongUnsafe(in, inOff)
        val c = Bits.getLongUnsafe(counterOut, i << LOG_LONG_BYTES)
        Bits.putBytes(out, outOff, (a ^ c) + shift(shiftCnt))
        shiftCnt += 1
        outOff += LONG_BYTES
        inOff += LONG_BYTES
        bytes += LONG_BYTES
      }

    }
    bytes
  }
}
