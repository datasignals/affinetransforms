package com.datasignals.affinetransforms.m2g

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.transformation.BlockTransformation
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Integer.{BYTES => INT_BYTES}

class Decrypt(private[this] val cipher: () => BlockCipher,
              private[this] val params: KeyParameter)
             (override val inBlockSize: Int) extends BlockTransformation {

  private[this] val nonceSize: Int = cipher().getBlockSize - INT_BYTES
  override val outBlockSize: Int = inBlockSize - nonceSize

  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    if(l <= nonceSize) return l

    val c = cipher()
    c.init(true, params)
    val cipherBlockSize = c.getBlockSize
    val counter = new Array[Byte](cipherBlockSize)
    System.arraycopy(in, inOffset, counter, 0, nonceSize)
    val counterOut = new Array[Byte](cipherBlockSize)

    val ll = l - nonceSize
    val subblocks = ll / cipherBlockSize
    val lastSubblockSize = ll % cipherBlockSize

    var inOff: Int = inOffset + nonceSize
    var outOff = outOffset
    var bytes = nonceSize
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
