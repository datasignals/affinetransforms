package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{
  ArrayIndex,
  Bits,
  DefradingParameters
}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{
  KeyInfo,
  KeyStoreManager,
  KeyStorePathInfo
}
import com.datasignals.affinetransforms.transformation.{
  DecryptAndUnshift,
  DynamicMatrixMixer,
  DynamicMatrixSplitter,
  EncryptAndShift
}
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.nio.file.Paths
import java.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}
import java.lang.Integer.{BYTES => INT_BYTES}

//Input Array    42,73,-98,-65,32,-115,44,124,107,109,-75,-8,55,116,120,-75,-10,-80,67,1,109,8,74,102,-58,-12,51,36,77,62,38,64,-33,102,50,16,-35,7,21,126,95,-123,-29,-72,-54,-95,72,106,-6,17,-9,-22,-41,17,-14,-104,-89,124,-84,87,40,50,107,-72,112,-29,-56,-31,75,27,5,66,-26,40,68,29,36,-58,27,58,2,-83,-99,105,92,-85,-26,18,-99,-69,20,-64,76,110,101,-46,-102,23,-95,65,-116,-93,12,82,101,-109,-128,70,63,-39,67,-28
//Result Array   0,0,0,12,0,0,0,23,0,3,34,-24,0,0,0,0,99,15,-10,2,22,106,-59,0,0,0,0,24,0,84,0,104,0,117,0,32,0,48,0,48,0,58,0,52,0,51,0,58,0,51,0,48,66,-120,0,0,66,-119,0,0,127,-64,0,0,127,-64,0,0,37,33,37,9

object Main {

  def main(args: Array[String]) = {}

  /** *****************************************************************************************************************
    */
  private[this] val dim = 2
  private[this] val keyStorePath =
    Paths.get("/Users/og_pixel/.m2g-data-viewer/keystore")
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

  val defradingParameters =
    DefradingParameters.apply(
      dim,
      keyStoreManager,
      "m2g_frading",
      "m2g_frading",
      "m2g_frading"
    )

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


  private val decryptAndUnshiftClass =
    new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)



  private val encryptAndShiftClass =
    new EncryptAndShift(cipherFactory, keyParam, new Random())(
      shift
    ) //(shiftArray)

  //TODO get this matrix somehow differently, I should not need
  // defradingParameters
  //  val nativeMatrixMixer = new NativeMatrixMixer(dim, defradingParameters.matrix)
  private val dynamicMatrixMixer = DynamicMatrixMixer(dim, defradingParameters.matrix)
  private val dynamicMatrixSplitter =
    DynamicMatrixSplitter(dim, defradingParameters.matrix)

  def decryptAndUnshift(in: Array[Byte]): Option[Array[Byte]] = {
    try {
//      val out = new Array[Byte](72) // Adjust the size as needed
//      val out = new Array[Byte]((24 * 2) + 16) // Adjust the size as needed

//      val out = new Array[Byte]((in.length * 2) + 16) // Adjust the size as needed

      val out = new Array[Byte]((in.length / 2) + 16 + 8) //TODO used different value here too
//      println("in len: " + in.length)


      var outOffset = 0
      var inOffset = 0
      var processedBytes = 24
      //TODO while loop possibly not needed

      //(
      //0, 0, 0, 10, 0, 0, 0, 26, 0, 0, 7, 62, 0, 0, 0, 0, 101, -27, -49, 125, 48, -25, -11, 96,
      //0, 0, 0, 38, 0, 84, 0, 101, 0, 115, 0, 116, 0, 32, 0, 85, 0, 110, 0, 112, 0, 97, 0, 114,
      //0, 115, 0, 101, 0, 100, 0, 32, 0, 69, 0, 118, 0, 101, 0, 110, 0, 116, -49, 49, 56, 89, 86, -117
      // )

      //0, 0, 0, 12, 0, 0, 0, 23, 0, 3, 34, -24, 0, 0, 0, 0, 99, 15, -10, 2, 22, 106, -59, 0, 0, 0, 0,
      //24, 0, 84, 0, 104, 0, 117, 0, 32, 0, 48, 0, 48, 0, 58, 0, 52, 0, 51, 0, 58, 0, 51, 0, 48, 66,
      //-120, 0, 0, 66, -119, 0, 0, 127, -64, 0, 0, 127, -64, 0, 0, 37, 33, 37, 9,
      //56 elements
      //TODO Iam 90% sure my current problem is due to lacking loop,
      // it seems to be working fine otherwise
      // even previously working event has no data now
      while (processedBytes == 24) {
//      var iterations = 0
//      while (iterations < 8) {
        processedBytes =
          decryptAndUnshiftClass.processBlock(out, outOffset, in, inOffset, 24)
//        println("process bytes: " + processedBytes)
        outOffset += 16
        inOffset += 24
//        iterations += 1
//      }
      }

      Some(out)
    } catch {
      case e: Exception =>
        println(s"Failed: ${e.printStackTrace()}")
        None
    }

  }

  def newDecryptAndUnshift(in: Array[Byte]): Array[Byte] = {
    decryptAndUnshiftClass.apply(in)
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
//    val out: ArrayIndex[Byte] = new ArrayIndex[Byte](new Array(112), 0, 112)
//    val out: ArrayIndex[Byte] = new ArrayIndex[Byte](new Array(24 * 2), 0, 24 * 2)

    //TODO still considers only two dimensions
    val out: ArrayIndex[Byte] = new ArrayIndex[Byte](new Array(in(0).length * 2), 0, in(0).length * 2)

    dynamicMatrixMixer.apply(out, in)

    out.array
  }

  //Formerly Splitter
  def disassemble(in: ArrayIndex[Byte]): Array[ArrayIndex[Byte]] = {
    val out: Array[ArrayIndex[Byte]] = Array(
      //TODO this division by two might be problematic
      //TODO I also divide by two because I expect two Shares
      new ArrayIndex[Byte](new Array(in.length / 2), 0, in.length / 2),
      new ArrayIndex[Byte](new Array(in.length / 2), 0, in.length / 2)
    )

    dynamicMatrixSplitter.apply(out, in)
    out
  }

  //TODO I think m2g-data-viewer
  // is doing some extra step before it can be
  // deserialised, this is this function
  def mysteryFunction(
      in: Array[Byte], /*, preMixedArray: Array[ArrayIndex[Byte]]*/
      mixLen: Int
  ): Array[Byte] = {
//    val ftotal = preMixedArray(0).length

//    val ftotal = 56 //this is just what I found when running m2g-data-viewer, this might be inconsitent
//    val ftotal = 24//this is just what I found when running m2g-data-viewer, this might be inconsitent

//    println("in length: " + ((in.length / 2) - (32)))
//    val ftotal = in.length//this is just what I found when running m2g-data-viewer, this might be inconsitent

//    val ftotal = (in.length / 2) - 32

    val ftotal = mixLen //From split array len


    val total = dim * ftotal

    //LR = INT_BYTES so INT_BYTES - INT_BYTES = 0
    val r = Bits.getIntUnsafe(in, 0)
//    val n = t.length(total - INT_BYTES + (if(r > 0) r - d else 0))

//    val n = 64 //this is just what I found when running m2g-data-viewer, this might be inconsitent
//    val n = 24 //this is just what I found when running m2g-data-viewer, this might be inconsitent
//    val n = in.length //this is just what I found when running m2g-data-viewer, this might be inconsitent
//    val n = (in.length / 2) - 32 //this is just what I found when running m2g-data-viewer, this might be inconsitent

    val n = in.length - 4// TODO this value is wrong all the time
    // Minus 8 because int_bytes is 8

    val value = new Array[Byte](n)
    System.arraycopy(in, INT_BYTES, value, 0, n)

    value
  }


  def fixLength(in: Array[Byte]): Array[Byte] =
    in.reverse.dropWhile(_ == 0).reverse

}
