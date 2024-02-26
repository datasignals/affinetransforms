package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager, KeyStorePathInfo}
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.nio.file.Paths
import java.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}



//Input Array    42,73,-98,-65,32,-115,44,124,107,109,-75,-8,55,116,120,-75,-10,-80,67,1,109,8,74,102,-58,-12,51,36,77,62,38,64,-33,102,50,16,-35,7,21,126,95,-123,-29,-72,-54,-95,72,106,-6,17,-9,-22,-41,17,-14,-104,-89,124,-84,87,40,50,107,-72,112,-29,-56,-31,75,27,5,66,-26,40,68,29,36,-58,27,58,2,-83,-99,105,92,-85,-26,18,-99,-69,20,-64,76,110,101,-46,-102,23,-95,65,-116,-93,12,82,101,-109,-128,70,63,-39,67,-28
//Result Array   0,0,0,12,0,0,0,23,0,3,34,-24,0,0,0,0,99,15,-10,2,22,106,-59,0,0,0,0,24,0,84,0,104,0,117,0,32,0,48,0,48,0,58,0,52,0,51,0,58,0,51,0,48,66,-120,0,0,66,-119,0,0,127,-64,0,0,127,-64,0,0,37,33,37,9


object Main {

  private val inputArray: Array[Byte] = Array(
    42, 73, -98, -65, 32, -115, 44, 124, 107, 109, -75, -8, 55, 116, 120, -75,
    -10, -80, 67, 1, 109, 8, 74, 102, -58, -12, 51, 36, 77, 62, 38, 64, -33,
    102, 50, 16, -35, 7, 21, 126, 95, -123, -29, -72, -54, -95, 72, 106, -6, 17,
    -9, -22, -41, 17, -14, -104, -89, 124, -84, 87, 40, 50, 107, -72, 112, -29,
    -56, -31, 75, 27, 5, 66, -26, 40, 68, 29, 36, -58, 27, 58, 2, -83, -99, 105,
    92, -85, -26, 18, -99, -69, 20, -64, 76, 110, 101, -46, -102, 23, -95, 65,
    -116, -93, 12, 82, 101, -109, -128, 70, 63, -39, 67, -28
  )

  private val resultArray: Array[Byte] = Array(
    0, 0, 0, 12, 0, 0, 0, 23, 0, 3, 34, -24, 0, 0, 0, 0, 99, 15, -10, 2, 22,
    106, -59, 0, 0, 0, 0, 24, 0, 84, 0, 104, 0, 117, 0, 32, 0, 48, 0, 48, 0, 58,
    0, 52, 0, 51, 0, 58, 0, 51, 0, 48, 66, -120, 0, 0, 66, -119, 0, 0, 127, -64,
    0, 0, 127, -64, 0, 0, 37, 33, 37, 9
  )

  //Part used for Mixer
  private val inputValue1: Array[Byte] = Array(
    74, -51, 32, 22, 89, -42, 52, -112, 42, 70, -2, 91, 67, -93, -73, 99, 82,
    81, -100, -116, -60, 123, -36, -24, -95, -68, 60, 0, -96, -28, -59, 22, 101,
    111, 63, 53, -33, -117, 81, -69, -6, -57, 46, 123, 55, 77, 118, 73, -71,
    -124, 47, 88, -79, 90, -2, 83
  )

  private val inputValue2: Array[Byte] = Array(
    -60, -46, 31, -75, 42, 15, -79, -32, 111, 19, -82, 28, 15, 24, -122, -100, //16,32,48,57
    103, 122, -125, 120, 60, -2, 57, -53, -65, 87, 125, -62, 34, -56, 66, 108,
    -38, -127, -32, -99, 67, 60, 93, -100, -75, 126, -6, -58, -69, -74, -33,
    -26, 100, 104, 4, 75, -74, -97, -36, -25
  )

  //ArrayIndex used for mixing, it represents a message split two ways
  private val arrayIndex: Array[ArrayIndex[Byte]] = Array(
    new ArrayIndex[Byte](inputValue1, 0, inputValue1.length),
    new ArrayIndex[Byte](inputValue2, 0, inputValue2.length),
  )


  def main(args: Array[String]) = {}

  /** *****************************************************************************************************************
    */
  private[this] val dim = 2
  private[this] val keyStorePath = Paths.get("/Users/og_pixel/.m2g-data-viewer/keystore")
  private[this] val keyStoreManager = new KeyStoreManager(
    KeyStorePathInfo(keyStorePath, "m2g".toCharArray)
  )

  private[this] val d = dim << LOG_LONG_BYTES
  private[this] val shift = new Array[Long](dim)
  Bits.getLongs(
    shift,
    0,
    keyStoreManager.getRawKey(KeyInfo("shift", "m2g_frading".toCharArray)),
    0,
    d
  )

  private[this] val key =
    keyStoreManager.getRawKey(KeyInfo("encrypt", "m2g_frading".toCharArray))
  private[this] val matrix = new Array[Long](dim * dim)

  Bits.getLongs(
    matrix,
    0,
    keyStoreManager.getRawKey(KeyInfo("frading", "m2g_frading".toCharArray)),
    0,
    dim * d
  )

  val cipherFactory = () => new AESEngine()

  private val cipher: () => BlockCipher = cipherFactory

  val keyParam = new KeyParameter(key)
  //  val decryptAndUnshift = new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)

  val filePath =
    "defraded.csv" //"ne-part-defraded.csv" //"df2.csv" //"defraded.csv"


  val defradingParameters =
    DefradingParameters.apply(dim, keyStoreManager, "m2g_frading", "m2g_frading", "m2g_frading")

  /** *****************************************************************************************************************
    */

  private[this] val cipherBlockSize: Int = cipher().getBlockSize
  private[this] val cipherSubblocks: Int = cipherBlockSize >> LOG_LONG_BYTES
  println(s"cipherSubblocks $cipherSubblocks\n")
  private[this] val nonceSize: Int = cipherBlockSize - LONG_BYTES
  private[this] val outSubblocks: Int = shift.length
  println(s"outSubblocks $outSubblocks\n")
  assume(cipherBlockSize % LONG_BYTES == 0)

  val outBlockSize: Int = outSubblocks << LOG_LONG_BYTES
  val inBlockSize: Int = outBlockSize + nonceSize

  val params = new KeyParameter(key)


  private val decryptAndUnshiftClass = new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)
  private val encryptAndShiftClass = new EncryptAndShift(cipherFactory,
    keyParam, new Random())(shift) //(shiftArray)


  //TODO get this matrix somehow differently, I should not need
  // defradingParameters
  val nativeMatrixMixer = new NativeMatrixMixer(dim, defradingParameters.matrix)

  def decryptAndUnshift(in: Array[Byte]): Option[Array[Byte]] = {
    try {
      val out = new Array[Byte](72) // Adjust the size as needed
      var outOffset = 0
      var inOffset = 0
      var processedBytes = 0
      while (processedBytes != 16) {
        processedBytes =
          decryptAndUnshiftClass.processBlock(out, outOffset, in, inOffset, 24)
        outOffset += 16
        inOffset += 24
      }

      Some(out)
    } catch {
      case e: Exception =>
        println(s"Failed: ${e.printStackTrace()}")
        None
    }

  }

  def encryptAndShift(in: Array[Byte]): Option[Array[Byte]] = {
    try {
      val out = new Array[Byte](112) // Adjust the size as needed
      var outOffset = 0
      var inOffset = 0
      var processedBytes = 0

      //TODO I am not sure exactly why, but counting by
      // "processed blocks" is not working well
      // Instead I think it should do 112 / 16 = 7
      var iterations = 0
      while (iterations < 8) {
        processedBytes =
          encryptAndShiftClass.processBlock(out, outOffset, in, inOffset, 16)
        outOffset += 24
        inOffset += 16
        iterations += 1
      }

      Some(out)
    } catch {
      case e: Exception =>
        println(s"Failed: ${e.printStackTrace()}")
        None
    }
  }


  //Formerly Mixer
  def assemble(in: Array[ArrayIndex[Byte]]): Array[Byte] = {
    val out: ArrayIndex[Byte] = new ArrayIndex[Byte](new Array(112), 0, 112)

    nativeMatrixMixer.apply(out, in)

    out.array
  }

  //Formerly Splitter
  def disassemble() = {}





  //Based on Decrypt and Unshift function with a loop to process the entire thing
  //TODO  I did this first but I think the second decrypt has better odds of working
//  def decrypt(in: Array[Byte]): Option[Array[Byte]] = {
//    val result: ArrayBuffer[Byte] = new ArrayBuffer()
//
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//
//    while (processedBytes != 16) {
//      val l = Math.min(length, in.length - inOffset)
//      if (l <= nonceSize) return None
//
//      val c = cipher()
//      c.init(true, params)
//      val counter = new Array[Byte](cipherBlockSize)
//      System.arraycopy(in, inOffset, counter, 0, nonceSize)
//      val counterOut = new Array[Byte](cipherBlockSize)
//
//      val ll = l - nonceSize
//      var subblocks =
//        (ll / cipherBlockSize) + (if (ll % cipherBlockSize == 0) 0 else 1)
//
//      var inOff: Int = inOffset + nonceSize
//      var outOff = outOffset
//      var bytes = nonceSize
//      var shiftCnt = 0
//      subblocks = 1 //HARD CODED!!!!!!!!!!!!!!!!!
//      for (k <- 0 until subblocks) {
//        Bits.putBytes(counter, nonceSize, k)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for (i <- 0 until cipherSubblocks) {
//          if (bytes >= l) return None
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
//      result.appendedAll(out)
//      outOffset += 16
//      inOffset += 24
//    }
//
//    Some(result.toArray)
//  }
//
//  //Based on Decrypt class alone, with a loop to process the entire thing
//  def decrypt2(in: Array[Byte]): Array[Byte] = {
//
//    val result: ArrayBuffer[Byte] = new ArrayBuffer()
//
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//
//    while (processedBytes != 16) {
//
//      val l = Math.min(length, in.length - inOffset)
//      if (l <= nonceSize) return out//l
//
//      val c = cipher()
//      c.init(true, params)
//      val cipherBlockSize = c.getBlockSize
//      val counter = new Array[Byte](cipherBlockSize)
//      System.arraycopy(in, inOffset, counter, 0, nonceSize)
//      val counterOut = new Array[Byte](cipherBlockSize)
//
//      val ll = l - nonceSize
//      val subblocks = ll / cipherBlockSize
//      val lastSubblockSize = ll % cipherBlockSize
//
//      var inOff: Int = inOffset + nonceSize
//      var outOff = outOffset
//      var bytes = nonceSize
//      for (k <- 0 until subblocks) {
//        Bits.putBytes(counter, nonceSize, k)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for (i <- 0 until cipherBlockSize) {
//          out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
//          outOff += 1
//          inOff += 1
//        }
//        bytes += cipherBlockSize
//      }
//
//      if (lastSubblockSize > 0) {
//        Bits.putBytes(counter, nonceSize, subblocks)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for (i <- 0 until lastSubblockSize) {
//          out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
//          outOff += 1
//          inOff += 1
//        }
//        bytes += lastSubblockSize
//      }
//      processedBytes += bytes
//    }
//
//    out
//  }
//
//  //Based on Unshift class in M2G
//  def unshift(in: Array[Byte]): Array[Byte] = {
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//
//    while (processedBytes != 16) {
//      val l = Math.min(length, in.length - inOffset)
//      var outOff = outOffset
//      var bytes = nonceSize
//      for (k <- 0 until outSubblocks) {
//        if (bytes >= l) return out
//        Bits.putBytes(out, outOff, Bits.getLongUnsafe(in, inOffset + bytes) - shift(k))
//        outOff += LONG_BYTES
//        bytes += LONG_BYTES
//      }
//
//      outOffset += 16
//      inOffset += 24
//      processedBytes += bytes
//    }
//    out
//  }
//
//  //Based on Shift class
//  def shift(in: Array[Byte]): Array[Byte] = {
//    /*********From Class*/
//    val inSubblocks: Int = shift.length
//    /*********/
//
//    /********From Snippet*/
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//    /*********/
//
//    while (processedBytes != 16) {
//      val l = Math.min(length, in.length - inOffset)
//      var outOff = outOffset
//      var bytes = 0
//      for (k <- 0 until inSubblocks) {
//        if (bytes >= l) return out
//        Bits.putBytes(out, outOff, Bits.getLongUnsafe(in, inOffset + bytes) + shift(k))
//        outOff += LONG_BYTES
//        bytes += LONG_BYTES
//      }
//      /********From Snippet to iterate correctly(?)*/
//      outOffset += 16
//      inOffset += 24
//      processedBytes += bytes
//      /*********/
//    }
//    out
//  }
//
//  def encrypt(in: Array[Byte]): Option[Array[Byte]] = {
//    /********From Snippet*/
//    val out = new Array[Byte](72) // Adjust the size as needed
//    val length = 24
//    var outOffset = 0
//    var inOffset = 0
//    var processedBytes = 0
//    val random = new Random()
//    /*********/
//
//
//
//
//    while (processedBytes != 16) {
//
//
//      val l = Math.min(length, in.length - inOffset)
//      if (l <= 0) return None
//
//      val iv = new Array[Byte](nonceSize)
//      random.nextBytes(iv)
//      System.arraycopy(iv, 0, out, outOffset, nonceSize)
//
//      val c = cipher()
//      c.init(true, params)
//      val cipherBlockSize = c.getBlockSize
//      val counter = new Array[Byte](cipherBlockSize)
//      System.arraycopy(iv, 0, counter, 0, nonceSize)
//      val counterOut = new Array[Byte](cipherBlockSize)
//
//      val subblocks = l / cipherBlockSize
//      val lastSubblockSize = l % cipherBlockSize
//
//      var outOff = outOffset + nonceSize
//      var inOff = inOffset
//      var bytes = 0
//      for (k <- 0 until subblocks) {
//        Bits.putBytes(counter, nonceSize, k)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for (i <- 0 until cipherBlockSize) {
//          out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
//          outOff += 1
//          inOff += 1
//        }
//        bytes += cipherBlockSize
//      }
//
//      if (lastSubblockSize > 0) {
//        Bits.putBytes(counter, nonceSize, subblocks)
//        c.reset()
//        c.processBlock(counter, 0, counterOut, 0)
//
//        for (i <- 0 until lastSubblockSize) {
//          out(outOff) = (in(inOff) ^ counterOut(i)).asInstanceOf[Byte]
//          outOff += 1
//          inOff += 1
//        }
//        bytes += lastSubblockSize
//      }
//
//      /********From Snippet to iterate correctly(?)*/
//      outOffset += 16
//      inOffset += 24
//      processedBytes += bytes
//      /*********/
//    }
//
//    Some(out)
//  }

}
