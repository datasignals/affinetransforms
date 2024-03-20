package com.datasignals.affinetransforms.transformation

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.util.Random
import java.util.concurrent.{Callable, Future}

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





















//  val BlocksPerThread: Int = 1 << 16 //65536
  val BlocksPerThread: Int = 1 << 10
//  val BlocksPerThread: Int = 1 //TODO one seems to work

  private[this] val blocksPerThread: Int = BlocksPerThread //TODO this is assumed based on a constructor default value using it

  @inline private def nFutures(nBlocks: Int): Int = {
    val k = nBlocks / blocksPerThread
    val r = nBlocks % blocksPerThread
    if(r > 0) k + 1 else k
  }

  def apply(data: Array[Byte]): Array[Byte] = {
    val inBlockSize = this.inBlockSize
    val outBlockSize = this.outBlockSize
    val info = GenericBlockTransformation.blockInfo(data.length, inBlockSize, outBlockSize)
    val nBlocks = info.numberOfBlocks
    val out = new Array[Byte](info.outLength)
    val fLength = nFutures(nBlocks)

    var outOffset = 0
    var inOffset = 0
    val inLength = blocksPerThread * inBlockSize
    val outLength = blocksPerThread * outBlockSize

    var i = 0
    while (i < fLength) {
      this.processBlock(out, outOffset, data, inOffset)
      outOffset += outLength
      inOffset += inLength
      i += 1
    }
    i = 0
    out
  }

  def length(inputLength: Int): Int =
    GenericBlockTransformation.blockInfo(inputLength, this.inBlockSize, this.outBlockSize).outLength


  def alignedLength(length: Int): Int = {
    val aligned = (length >> LOG_LONG_BYTES) << LOG_LONG_BYTES
    if(aligned == length) return length
    aligned + LONG_BYTES
  }
}
