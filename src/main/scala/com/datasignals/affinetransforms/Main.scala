package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits, DefradingParameters}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager, KeyStorePathInfo}
import com.datasignals.affinetransforms.transformation.{DecryptAndUnshift, DynamicMatrixMixer, DynamicMatrixSplitter, EncryptAndShift}
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter

import java.lang.Long.{BYTES => LONG_BYTES}
import java.nio.file.Paths
import java.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}
import java.lang.Integer.{BYTES => INT_BYTES}
import scala.concurrent.Future
import scala.util.control.NonFatal

//Input Array    42,73,-98,-65,32,-115,44,124,107,109,-75,-8,55,116,120,-75,-10,-80,67,1,109,8,74,102,-58,-12,51,36,77,62,38,64,-33,102,50,16,-35,7,21,126,95,-123,-29,-72,-54,-95,72,106,-6,17,-9,-22,-41,17,-14,-104,-89,124,-84,87,40,50,107,-72,112,-29,-56,-31,75,27,5,66,-26,40,68,29,36,-58,27,58,2,-83,-99,105,92,-85,-26,18,-99,-69,20,-64,76,110,101,-46,-102,23,-95,65,-116,-93,12,82,101,-109,-128,70,63,-39,67,-28
//Result Array   0,0,0,12,0,0,0,23,0,3,34,-24,0,0,0,0,99,15,-10,2,22,106,-59,0,0,0,0,24,0,84,0,104,0,117,0,32,0,48,0,48,0,58,0,52,0,51,0,58,0,51,0,48,66,-120,0,0,66,-119,0,0,127,-64,0,0,127,-64,0,0,37,33,37,9

object Main {

  def main(args: Array[String]) = {}

  private[this] val LR = INT_BYTES
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


  val random = new Random()

  private val encryptAndShiftClass =
    new EncryptAndShift(cipherFactory, keyParam, random)(
      shift
    ) //(shiftArray)

  //TODO get this matrix somehow differently, I should not need
  // defradingParameters
  //  val nativeMatrixMixer = new NativeMatrixMixer(dim, defradingParameters.matrix)
  private val dynamicMatrixMixer = DynamicMatrixMixer(dim, defradingParameters.matrix)
  private val dynamicMatrixSplitter =
    DynamicMatrixSplitter(dim, defradingParameters.matrix)

  //TODO fully working best version
  def decryptAndUnshift(in: Array[Byte]): Array[Byte] = {
    val decrypted = decryptAndUnshiftClass.apply(in)


    //This was part of the "mystery" function
    val ftotal = in.length / 2
    val total = dim * ftotal

    val r = Bits.getIntUnsafe(decrypted, LR - INT_BYTES)

    val n = decryptAndUnshiftClass.length(total - LR + (if(r > 0) r - d else 0))

    val value = new Array[Byte](n)
    System.arraycopy(decrypted, LR, value, 0, n)
    value
  }

  //Formerly Mixer
  def assemble(in: Array[ArrayIndex[Byte]]): Array[Byte] = {
    //TODO still considers only two dimensions
    val out: ArrayIndex[Byte] = new ArrayIndex[Byte](new Array(in(0).length * dim), 0, in(0).length * dim)

    dynamicMatrixMixer.apply(out, in)

    out.array
  }

  def newEncryptAndShift(in: Array[Byte]): (Array[Byte], Int) = {
    //TODO I think this part works fine
    val encryptedData = encryptAndShiftClass.apply(in)

    val value = encryptedData

    val n = value.length
    val nlr0 = n + LR

    val nlr1 = encryptAndShiftClass.alignedLength(nlr0)
    val nlr2 = encryptAndShiftClass.length(nlr1)

    val extraLength = nlr1 - nlr0

    val nlr = if(extraLength > 0) encryptAndShiftClass.length(nlr0) else nlr2
    val r = nlr % d
    val ftotal = ((nlr / d) + (if (r > 0) 1 else 0)) << LOG_LONG_BYTES
    val total = ftotal * dim


    val padl = total - nlr2

    val nlr0Array = new Array[Byte](nlr1)
    Bits.putBytes(nlr0Array, LR - INT_BYTES, r)
    System.arraycopy(value, 0, nlr0Array, LR, n)

    if(extraLength > 0) {
      val extra = new Array[Byte](extraLength)
      random.nextBytes(extra)
      System.arraycopy(extra, 0, nlr0Array, nlr0, extraLength)
    }


    val nlrArray = encryptAndShiftClass(nlr0Array)


    val inArray = new Array[Byte](total)
    System.arraycopy(nlrArray, 0, inArray, 0, nlr2)
    val pad = new Array[Byte](padl)
    random.nextBytes(pad)
    System.arraycopy(pad, 0, inArray, nlr2, padl)

    (inArray, ftotal)



//    val out = new Array[ArrayIndex[Byte]](dim)
//    for (i <- 0 until dim) {
//      out(i) = new ArrayIndex[Byte](new Array[Byte](ftotal), 0)
//    }
//
//    out

//    splitter(out, new ArrayIndex[Byte](inArray, 0))
//
//    for (i <- 0 until dim) {
//      addables(i).addAndSignal(new M2GSemiRawRecord(key, out(i).array))
//    }
//
//    new M2GSemiRawRecord(key, out(i).array)
  }



  //Formerly Splitter
  def disassemble(in: Array[Byte], ftotal: Int): Array[ArrayIndex[Byte]] = {
    val out: Array[ArrayIndex[Byte]] = Array(
      //TODO this division by two might be problematic
      //TODO I also divide by two because I expect two Shares
      new ArrayIndex[Byte](new Array[Byte](ftotal), 0),
      new ArrayIndex[Byte](new Array[Byte](ftotal), 0)
    )

    dynamicMatrixSplitter.apply(out, new ArrayIndex[Byte](in, 0))
    out
  }

  def newDisassemble(in: ArrayIndex[Byte]): Array[ArrayIndex[Byte]] = {
    val out: Array[ArrayIndex[Byte]] = Array(
      //TODO this division by two might be problematic
      //TODO I also divide by two because I expect two Shares
      new ArrayIndex[Byte](new Array(in.length / 2), 0, in.length / 2),
      new ArrayIndex[Byte](new Array(in.length / 2), 0, in.length / 2)
    )

    dynamicMatrixSplitter.apply(out, in)
    out
  }

}
