package com.datasignals.affinetransforms.m2g

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.transformation.BlockTransformation
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Integer.{BYTES => INT_BYTES}
import java.util.Random

class Encrypt(private[this] val cipher: () => BlockCipher,
              private[this] val params: KeyParameter,
              private[this] val random: Random)
             (override val inBlockSize: Int) extends BlockTransformation {

  private[this] val nonceSize: Int = cipher().getBlockSize - INT_BYTES
  override val outBlockSize: Int = inBlockSize + nonceSize

  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    if(l <= 0) return 0

    val iv = new Array[Byte](nonceSize)
    random.nextBytes(iv)
    System.arraycopy(iv, 0, out, outOffset, nonceSize)

    val c = cipher()
    c.init(true, params)
    val cipherBlockSize = c.getBlockSize
    val counter = new Array[Byte](cipherBlockSize)
    System.arraycopy(iv, 0, counter, 0, nonceSize)
    val counterOut = new Array[Byte](cipherBlockSize)

    val subblocks = l / cipherBlockSize
    val lastSubblockSize = l % cipherBlockSize

    var outOff = outOffset + nonceSize
    var inOff = inOffset
    var bytes = 0
    for(k <- 0 until subblocks) {
      Bits.putBytes(counter, nonceSize, k)
      c.reset()
      c.processBlock(counter, 0, counterOut, 0)

      for(i <- 0 until cipherBlockSize) {
        out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
        outOff += 1
        inOff += 1
      }
      bytes += cipherBlockSize
    }

    if(lastSubblockSize > 0) {
      Bits.putBytes(counter, nonceSize, subblocks)
      c.reset()
      c.processBlock(counter, 0, counterOut, 0)

      for(i <- 0 until lastSubblockSize) {
        out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
        outOff += 1
        inOff += 1
      }
      bytes += lastSubblockSize
    }

    bytes
  }
}
