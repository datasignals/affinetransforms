package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager, KeyStorePathInfo}
import com.datasignals.affinetransforms.transformation.{DecryptAndUnshift, EncryptAndShift}
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter
import utest.{TestSuite, Tests, test}

import java.nio.file.Paths
import java.util.Random
import scala.io.Source
import scala.util.Try

object Spec extends TestSuite with Data {


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

  //val keyParam = new KeyParameter(keyBytes)
  val keyParam = new KeyParameter(key)
  // Step 4: Instantiate DecryptAndUnshift
  val decryptAndUnshift =
    new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)

  private val encryptAndShift = new EncryptAndShift(cipherFactory,
    keyParam, new Random())(shift) //(shiftArray)

  override val tests: Tests = Tests {

    test("Basic Decrypt and Unshift") - {
//      val res = Main.decryptAndUnshift(inputArray)
//      println("Result: " + res.getOrElse("Failed").asInstanceOf[Array[Byte]].mkString("", " ,", ""))
//      println("Expected: " + resultArray.mkString("", ", ", ""))
      assert(
        Main.decryptAndUnshift(inputArray)
          .fold(false)(_.sameElements(resultArray))
      )

    }


    test("Decrypt and Unshift, then Encrypt and Shift Back") - {
      val decryptResult = Main.decryptAndUnshift(inputArray)

      val encryptResult = decryptResult.flatMap { decryptValue =>
        Main.encryptAndShift(decryptValue)
      }

      val decryptResult2 = encryptResult.flatMap { encryptValue =>
        Main.decryptAndUnshift(encryptValue)
      }

      decryptResult2 match {
        case Some(value) =>
//          println("Decrypted again: " + value.mkString("", ", ", ""))
//          println("Expected: " + resultArray.mkString("", ", ", ""))
          assert(value.sameElements(resultArray))
        case None => assert(false)
      }
    }


    test("Mixing Test") - {
      val mixResult = Main.assemble(arrayIndex)

      val decryptAndUnshiftResult = Main.decryptAndUnshift(mixResult)

      decryptAndUnshiftResult.foreach { value =>
        println("Result: " + value.mkString("Array(", ", ", ")"))
      }

      //TODO no value to compare it to, but return result has 0,0,0,12.
      // Indicating it should be correct
    }

    test("Splitting Test") - {
      val disassembleResult = Main.disassemble(
        new ArrayIndex(inputArray, 0, inputArray.length)
      )

      val assembleBack = Main.assemble(
        disassembleResult
      )

      val decryptAndUnshift = Main.decryptAndUnshift(
        assembleBack
      )

      println("Input: " + inputArray.mkString("Array(", ", ", ")"))
      println("Decrypted: " + decryptAndUnshift.get.mkString("Array(", ", ", ")"))

//      disassembleResult.foreach { e =>
//        println("Disassemble: " + e.array.mkString("", ", ", ""))
//      }
//
//      println("Input: " + inputArray.mkString("Array(", ", ", ")"))
//      println("Result: " + assembleBack.mkString("Array(", ", ", ")"))
    }


//    test("Separate Decrypt and Unshift, old Functions") - {
//      val decrypt = new Decrypt(cipherFactory, keyParam)(16)
//      val unshift = new Unshift(shift)
//
//      var outOffset = 0
//      var inOffset = 0
//      var processedBytes = 0
//
//      val out = new Array[Byte](72) // Adjust the size as needed
//
//      while (processedBytes != 16) {
//        processedBytes =
//          decrypt.processBlock(out, outOffset, inputArray, 24)
//        outOffset += 16
//        inOffset += 24
//      }
//
//      outOffset = 0
//      inOffset = 0
//      processedBytes = 0
//      val out2 = new Array[Byte](72) // Adjust the size as needed
//
//      while (processedBytes != 16) {
//        processedBytes =
//          unshift.processBlock(out2, outOffset, out, 24)
//        outOffset += 16
//        inOffset += 24
//      }
//
//      println("Decrypt Result: " + out.mkString("", ", ", ""))
//      println("Unshift Result: " + out2.mkString("", ", ", ""))
//
//
//
//    }
//
//    test("Separate Decrypt and Unshift, looping Functions") - {
//      val decrypted = Main.decrypt(inputArray)
//      if(decrypted.isEmpty) {
//        assert(false)
//      }
//
//      println("Decrypted: " + decrypted.get.mkString("", ", ", ""))
//
//      val unshifted = Main.unshift(decrypted.get)
//
//
//
//      println("Result: " + unshifted.mkString("", ", ", ""))
//      println("Expected: " + resultArray.mkString("", ", ", ""))
//
//      assert(unshifted eq resultArray)
//    }
//
//    test("EncryptAndUnsift Class - With separate unshift") - {
//      val dim = 2
//      val keyStorePath = Paths.get("/Users/og_pixel/.m2g-data-viewer/keystore")
//      val keyStoreManager = new KeyStoreManager(KeyStorePathInfo(keyStorePath, "m2g".toCharArray))
//
//      val d = dim << LOG_LONG_BYTES
//      val shift = new Array[Long](dim)
//      Bits.getLongs(shift, 0, keyStoreManager.getRawKey(KeyInfo("shift", "m2g_frading".toCharArray)), 0, d)
//      val key = keyStoreManager.getRawKey(KeyInfo("encrypt", "m2g_frading".toCharArray))
//      val matrix = new Array[Long](dim * dim)
//      Bits.getLongs(matrix, 0, keyStoreManager.getRawKey(KeyInfo("frading", "m2g_frading".toCharArray)), 0, dim * d)
//      val cipherFactory = () => new AESEngine()
//
//      val keyParam = new KeyParameter(key)
//
//      val decryptAndUnshiftSeparate = new DecryptAndUnshiftSeparate(cipherFactory, keyParam)(shift) //(shiftArray)
//
//      val decrypted = decryptAndUnshiftSeparate.decrypt(inputArray)
//
//      println("Decrypted: " + decrypted.mkString("", ", ", ""))
//
//      val unshifted = decryptAndUnshiftSeparate.unshift(decrypted)
//
//      println("Unshifted: " + unshifted.mkString("", ", ", ""))
//      println("Expected: " + resultArray.mkString("", ", ", ""))
//
//    }



  }

}
