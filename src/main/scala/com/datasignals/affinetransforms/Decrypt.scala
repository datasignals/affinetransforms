package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.Bits
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager, KeyStorePathInfo}
import com.datasignals.affinetransforms.transformation.BlockTransformation
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.nio.file.Paths
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Try

//class Decrypt(private[this] val cipher: () => BlockCipher,
//                        private[this] val params: KeyParameter)
//                       /*(private[this] val shift: Array[Long])*/ extends BlockTransformation {
//
//
//  /********************************************************************************************************************/
//  private[this] val dim = 2
//  private[this] val keyStorePath = Paths.get("keystore")
//  private[this] val keyStoreManager = new KeyStoreManager(KeyStorePathInfo(keyStorePath, "m2g".toCharArray))
//
//  private[this] val d = dim << LOG_LONG_BYTES
//  private[this] val shift = new Array[Long](dim)
//  Bits.getLongs(shift, 0, keyStoreManager.getRawKey(KeyInfo("shift", "m2g_frading".toCharArray)), 0, d)
//
//  private[this] val key = keyStoreManager.getRawKey(KeyInfo("encrypt", "m2g_frading".toCharArray))
//  private[this] val matrix = new Array[Long](dim * dim)
//
//  Bits.getLongs(matrix, 0, keyStoreManager.getRawKey(KeyInfo("frading", "m2g_frading".toCharArray)), 0, dim * d)
//
//  val cipherFactory = () => new AESEngine()
//
//  val keyParam = new KeyParameter(key)
////  val decryptAndUnshift = new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)
//
//  val filePath = "defraded.csv" //"ne-part-defraded.csv" //"df2.csv" //"defraded.csv"
//  /********************************************************************************************************************/
//
//
//
//
//  private[this] val cipherBlockSize: Int = cipher().getBlockSize
//  private[this] val cipherSubblocks: Int = cipherBlockSize >> LOG_LONG_BYTES
//  println(s"cipherSubblocks $cipherSubblocks\n")
//  private[this] val nonceSize: Int = cipherBlockSize - LONG_BYTES
//  private[this] val outSubblocks: Int = shift.length
//  println(s"outSubblocks $outSubblocks\n")
//  assume(cipherBlockSize % LONG_BYTES == 0)
//
//  override val outBlockSize: Int = outSubblocks << LOG_LONG_BYTES
//  override val inBlockSize: Int = outBlockSize + nonceSize
//
//
//  def decryptAndUnshift(in: String): Option[Array[Byte]] =
//    this.decryptAndUnshift(in.split(",").map(_.trim.toByte))
//
//  def decryptAndUnshift(in: Array[Byte]/*, inOffset: Int, length: Int*/): Option[Array[Byte]] = {
//    val result: ArrayBuffer[Byte] = new ArrayBuffer[Byte]()
//
//    Try {
//        val out = new Array[Byte](72) // Adjust the size as needed
//        var outOffset = 0
//        var inOffset = 0
//        var processedBytes = 0
//        while (processedBytes != 16) {
//          processedBytes = this.processBlock(out, outOffset, in, inOffset, 24)
//          outOffset += 16
//          inOffset += 24
//        }
////        out.map(i => f"$i%03d").mkString("", ", ", "")
//        result += out
//    }.recover {
//      case e: Exception =>
//        println(s"Failed: ${e.printStackTrace()}")
//        Option.empty
//    }
//
//    Some(result.toArray)
//  }
//
//  //Possibly "simple" plug and receive result decrypt
//  def decrypt(in: Array[Byte]): Option[Array[Byte]] = {
//    val result: ArrayBuffer[Byte] = new ArrayBuffer()
//
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//
//
//    while (processedBytes != 16) {
//      val l = Math.min(length, in.length - inOffset)
//      if(l <= nonceSize) return None
//
//      val c = cipher()
//      c.init(true, params)
//      val counter = new Array[Byte](cipherBlockSize)
//      System.arraycopy(in, inOffset, counter, 0, nonceSize)
//      val counterOut = new Array[Byte](cipherBlockSize)
//
//      val ll = l - nonceSize
//      var subblocks = (ll / cipherBlockSize) + (if(ll % cipherBlockSize == 0) 0 else 1)
//
//      var inOff: Int = inOffset + nonceSize
//      var outOff = outOffset
//      var bytes = nonceSize
//      var shiftCnt = 0
//      subblocks = 1 //HARD CODED!!!!!!!!!!!!!!!!!
//      for(k <- 0 until subblocks) {
//        Bits.putBytes(counter, nonceSize, k)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for(i <- 0 until cipherSubblocks) {
//          if(bytes >= l) return None
//          val a = Bits.getLongUnsafe(in, inOff)
//          val c = Bits.getLongUnsafe(counterOut, i << LOG_LONG_BYTES)
//          Bits.putBytes(out, outOff, a)
//          shiftCnt += 1
//          outOff += LONG_BYTES
//          inOff += LONG_BYTES
//          bytes += LONG_BYTES
//        }
//        processedBytes += bytes
//      }
//
//      result += out
//      outOffset += 16
//      inOffset += 24
//    }
//
//    Some(result.toArray)
//  }
//
//  def unshift(in: Array[Byte]): Option[Array[Byte]] = {
//    val out: Array[Byte] = new Array(72)
//
//    var shiftCnt = 0
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var bytes = nonceSize
//    var inOff: Int = inOffset + nonceSize
//    var outOff = outOffset
//    val counterOut = new Array[Byte](cipherBlockSize)
//    val l = Math.min(length, in.length - inOffset)
//
//    var processedBytes = 0
//
//    while (processedBytes != 16) {
//      if (l <= nonceSize) return None
//
//      for (i <- 0 until cipherSubblocks) {
//        if (bytes >= l) return Some(out)
//        val a = Bits.getLongUnsafe(in, inOff)
//        val c = Bits.getLongUnsafe(counterOut, i << LOG_LONG_BYTES)
//        Bits.putBytes(out, outOff, (a - shift(shiftCnt)) ^ c)
//        shiftCnt += 1
//        outOff += LONG_BYTES
//        inOff += LONG_BYTES
//        bytes += LONG_BYTES
//      }
//      processedBytes += bytes
//    }
//
//    Some(out)
//  }
//
//
//
//  override def processBlock(out: Array[Byte], outOffset: Int, in: Array[Byte], inOffset: Int, length: Int): Int = {
//    val l = Math.min(length, in.length - inOffset)
//    if(l <= nonceSize) return l
//
//    val c = cipher()
//    c.init(true, params)
//    val counter = new Array[Byte](cipherBlockSize)
//    System.arraycopy(in, inOffset, counter, 0, nonceSize)
//    val counterOut = new Array[Byte](cipherBlockSize)
//
//    val ll = l - nonceSize
//    var subblocks = (ll / cipherBlockSize) + (if(ll % cipherBlockSize == 0) 0 else 1)
//
//    var inOff: Int = inOffset + nonceSize
//    var outOff = outOffset
//    var bytes = nonceSize
//    var shiftCnt = 0
//    subblocks = 1 //HARD CODED!!!!!!!!!!!!!!!!!
//    for(k <- 0 until subblocks) {
//      Bits.putBytes(counter, nonceSize, k)
//      c.reset()
//      c.processBlock(counter, 0, counterOut, 0)
//
//      for(i <- 0 until cipherSubblocks) {
//        if(bytes >= l) return bytes
//        val a = Bits.getLongUnsafe(in, inOff)
//        val c = Bits.getLongUnsafe(counterOut, i << LOG_LONG_BYTES)
//        Bits.putBytes(out, outOff, (a - shift(shiftCnt)) ^ c)
//        shiftCnt += 1
//        outOff += LONG_BYTES
//        inOff += LONG_BYTES
//        bytes += LONG_BYTES
//      }
//
//    }
//    bytes
//  }
//
//
//}
