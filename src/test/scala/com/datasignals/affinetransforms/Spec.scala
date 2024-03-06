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
import utest._

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

    test("new decrypt test") - {
      val merged = Main.assemble(longSplit)

      val assembledOldVersion = Main.decryptAndUnshift(merged)

      val aseemebleNewVersion = Main.newDecryptAndUnshift(merged)

//      println("Old: " + assembledOldVersion.get.mkString("", ", ", ""))
//      println("New: " + aseemebleNewVersion.mkString("", ", ", ""))

      val merged2 = Main.assemble(split)

      val assembledOldVersion2 = Main.decryptAndUnshift(merged2)

      val aseemebleNewVersion2 = Main.newDecryptAndUnshift(merged2)

//      println("Old2: " + assembledOldVersion2.get.mkString("", ", ", ""))
//      println("New2: " + aseemebleNewVersion2.mkString("", ", ", ""))

      println("xx: " + aseemebleNewVersion.length)
      assert(aseemebleNewVersion2.sameElements(decodedAndShifted))
    }

//    test("Mixing Test") - {
//      assert(
//        Main
//          .assemble(split)
//          .sameElements(merged)
//      )
//    }

//    test("Decrypt and Unshift Test") - {
//      println(
//        Main
//          .fixLength(
//            Main
//              .decryptAndUnshift(merged)
//              .get
//          )
//          .mkString("", ", ", "")
//      )
//
//      println(decodedAndShifted.mkString("", ", ", ""))

//      val a = Main.fixLength(
//        Main
//          .decryptAndUnshift(merged)
//          .get
//      )
//
//      assert(
//        a.sameElements(decodedAndShifted)
//      )
//    }

//    test("\"Magic\" Function") - {
//
//      println(
//        Main.mysteryFunction(decodedAndShifted).mkString("", ", ", "")
//      )
//      println(
//        afterMysteryFunction.mkString("", ", ", "")
//      )
//
//      assert(
//        Main
//          .mysteryFunction(decodedAndShifted)
//          .sameElements(afterMysteryFunction)
//      )
//    }

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

//    test("Full Decrypt - NO ASSERTION") - {
//      println("longSplit1 length: " + longSplit1.length)        // 80 makes sense
////      println("longMerge length: " + longSplit1.length)       // 160 -> 80 * 2 = 120 ----- makes sense
////      println("longDecoded length: " + longSplit1.length)     // 104 ???? 120-16
////      println("longMystery length: " + longSplit1.length)     // 100 ????
//
//      println("//////////")
//      println("split1 length: " + split1.length)                // 56 makes sense
//      println("merge length: " + merged.length)                 // 112 -> 56 * 2 = 112 ----- makes sense
//      println("decoded length: " + decodedAndShifted.length)    // 72 ????? 56 + 16 = 72 maybe ???? 112/2+16 = 72 I think ||| 112-40=72
//      println("mystery length: " + afterMysteryFunction.length) // 64 ???? 72 - 8 = 64 maybe
//      //Processed bytes is always 24
//
//
//      val assembleResult = Main.assemble(longSplit) //I believe this bit is right
//      println("assembled: " + assembleResult.mkString("", ", ", ""))
//      println("assembled len: " + assembleResult.length)
//
//      val decrypted = Main.decryptAndUnshift(assembleResult) //Added 8 for some reason, no good
//      println("decrypted: " + decrypted.get.mkString("", ", ", ""))
//      println("decrypted len: " + decrypted.get.length)
//
//      val mysteryValue = Main.mysteryFunction(decrypted.get, longSplit1.length)
//      println("mysterFun: " + mysteryValue.mkString("", ", ", ""))
//      println("mysterFun len: " + mysteryValue.length)
//
//      //TODO This is to make sure changes don't affect what already works
////      assert(mysteryValue.sameElements(afterMysteryFunction))
//    }

    test("Decrypt ALL types of Events") - {
      val translated: Array[Array[ArrayIndex[Byte]]] = ALL_EVENTS.map {
        eventFraded =>
          eventFraded.map { event =>
            new ArrayIndex[Byte](event, 0, event.length)
          }
      }

      translated.zipWithIndex.foreach { blockWithIndex =>
        val block = blockWithIndex._1
        val index = blockWithIndex._2
//        println("Block splits length: " + block(0).length)

        val mixed = Main.assemble(block)
//        println("mixed length: " + mixed.length)
        //TODO at this point everything is correct

        val decrypted = Main.newDecryptAndUnshift(mixed)
//        println("decrypted length: " + decrypted.get.length)
        if(mixed.length == 112) {
//          println("arr decrypted: " + decrypted.get.mkString("", ", ", ""))
        }
        println



        val shiftAgain = Main.newMysteryFunction(decrypted, block(0).length)
//        val result =
//          shiftAgain.map(e => e.mkString("", ", ", ""))

        println(s"${shiftAgain.mkString("", ", ","")}")
      }
    }

  }

}
