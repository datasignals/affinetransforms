package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{
  KeyInfo,
  KeyStoreManager,
  KeyStorePathInfo
}
import com.datasignals.affinetransforms.transformation.{
  DecryptAndUnshift,
  EncryptAndShift
}
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter
import utest.{TestSuite, Tests, test}

import java.nio.file.Paths
import java.util.Random
import scala.io.Source
import scala.util.Try

object Spec extends TestSuite with Data {

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

  //val keyParam = new KeyParameter(keyBytes)
  val keyParam = new KeyParameter(key)
  // Step 4: Instantiate DecryptAndUnshift
  val decryptAndUnshift =
    new DecryptAndUnshift(cipherFactory, keyParam)(shift) //(shiftArray)

  private val encryptAndShift =
    new EncryptAndShift(cipherFactory, keyParam, new Random())(
      shift
    ) //(shiftArray)

  override val tests: Tests = Tests {

    test("Mixing Test") - {
      assert(
        Main
          .assemble(split)
          .sameElements(merged)
      )
    }

    test("Decrypt and Unshift Test") - {
      assert(
        Main
          .decryptAndUnshift(merged)
          .fold(false)(_.sameElements(decodedAndShifted))
      )
    }

    test("\"Magic\" Function") - {
      assert(
        Main
          .mysteryFunction(decodedAndShifted)
          .sameElements(afterMysteryFunction)
      )
    }

    //TODO splitting doesn't seem to work
//    test("Splitting Test") - {
//      val disassembleResult = Main.disassemble(
//        new ArrayIndex(inputArray, 0, inputArray.length)
//      ).foreach(e => println(e.array.mkString("", ", ", "")))
//
//
//      val assembleBack = Main.assemble(
//        disassembleResult
//      )
//
//      val decryptAndUnshift = Main.decryptAndUnshift(
//        assembleBack
//      )
//
//      println("Input: " + inputArray.mkString("Array(", ", ", ")"))
//      println("Decrypted: " + decryptAndUnshift.get.mkString("Array(", ", ", ")"))
//
//      disassembleResult.foreach { e =>
//        println("Disassemble: " + e.array.mkString("", ", ", ""))
//      }
//
//      println("Input: " + inputArray.mkString("Array(", ", ", ")"))
//      println("Result: " + assembleBack.mkString("Array(", ", ", ")"))
//    }

    test("Full Decrypt - NO ASSERTION") - {
      val assembleResult = Main.assemble(split)

      val decrypted = Main.decryptAndUnshift(assembleResult)

      val mysteryValue = Main.mysteryFunction(decrypted.get)
    }

    test("Decrypt ALL types of Events with length 56") - {
      val translated: Array[Array[ArrayIndex[Byte]]] = ALL_EVENTS
        .map { eventFraded =>
          eventFraded.map { event =>
            new ArrayIndex[Byte](event, 0, event.length)
          }
        }
        .filter(
          _.forall(e => e.array.length == 56)
        ) //Only store events that have arrayIndex of length 56

      translated.foreach(arr => arr.foreach(e => println(e.array.mkString("", ", ", ""))))

      translated.zipWithIndex.foreach { blockWithIndex =>
        val block = blockWithIndex._1
        val index = blockWithIndex._2

        val mixed = Main.assemble(block)
        val decrypted = Main.decryptAndUnshift(mixed)
        val shiftAgain = decrypted.map(Main.mysteryFunction)
        val result = shiftAgain.map(e => e.mkString("", ", ", ""))

        println(s"Result $index: ${result.getOrElse("Failed")}")
      }
    }


    //TODO this will throw SEGFAULT from Mixing
//    test("Decrypt ALL types of Events") - {
//      val translated: Array[Array[ArrayIndex[Byte]]] = ALL_EVENTS.map {
//        eventFraded =>
//          eventFraded.map { event =>
//            new ArrayIndex[Byte](event, 0, event.length)
//          }
//      }
//
//      translated.zipWithIndex.foreach { blockWithIndex =>
//        val block = blockWithIndex._1
//        val index = blockWithIndex._2
//
//        val mixed = Main.assemble(block)
//        val decrypted = Main.decryptAndUnshift(mixed)
//        val shiftAgain = decrypted.map(Main.mysteryFunction)
//        val result = shiftAgain.map(e => e.mkString("", ", ", ""))
//
//        println(s"Result $index: ${result.getOrElse("Failed")}")
//      }
//    }

  }

}
