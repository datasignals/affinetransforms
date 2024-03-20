package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits}
import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
import com.datasignals.affinetransforms.keystore.{KeyInfo, KeyStoreManager, KeyStorePathInfo}
import com.datasignals.affinetransforms.transformation.{DecryptAndUnshift, EncryptAndShift}
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter
import utest._

import java.io.{File, FileWriter}
import java.nio.file.Paths
import java.util.{Random, Scanner}
import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

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


  def testRealData(source: String, dest: String) = {
    val fileWriter =
      new FileWriter(dest, true)

//    val file = new File(source)
//    val reader = new Scanner(file)
    val reader = Source.fromFile(source)

    var iterator = 0
//    while (reader.hasNextLine) {
    for(line <- reader.getLines()) {
//      val line = reader.nextLine()

      processLine(line) match {
        case Failure(exception) => println("error processing line: " + iterator + ". Reason: " + exception + " line: " + line)
        case Success(value) => fileWriter.append(value.mkString("", ", ", "") + "\n")
      }
      println(iterator)
      iterator = iterator + 1
    }
//    }

    fileWriter.close()
    reader.close()
  }


  def processLine(s: String): Try[Array[Byte]] = Try {
    val dataArray = s
      .split("\\|")
      .map(_.trim) // Splitting by '|' and trimming whitespaces

    val id = dataArray(0)
    val frade1 = dataArray(1).trim.split(",").map(e => e.toInt.toByte)
    val frade2 = dataArray(2).trim.split(",").map(e => e.toInt.toByte)
//    println("frade1: " + frade1.mkString("", ", ", ""))
//    println("frade2: " + frade2.mkString("", ", ", ""))
//    println

    val fullFrade = Array(
      new ArrayIndex[Byte](frade1, 0, frade1.length),
      new ArrayIndex[Byte](frade2, 0, frade2.length)
    )
    val assembled = Main.assemble(fullFrade)

    Main.decryptAndUnshift(assembled)
  }

  def testRealData() = {
    val fileWriter =
      new FileWriter("/Users/og_pixel/Desktop/small.decrypted", true)
    val path = "/Users/og_pixel/Desktop/small.frades"
    val testTxtSource = Source.fromFile(path)
//    val str = testTxtSource.mkString
//    testTxtSource.close()
    val lines = testTxtSource.getLines()
//    val len = lines.length
    println("lines: " + lines.length)

    try {
      for (i <- 0 to 10) {
//        val z = Try {
          val line = lines.next()

          val dataArray = line
            .split("\\|")
            .map(_.trim) // Splitting by '|' and trimming whitespaces

          val id = dataArray(0)
          val frade1 = dataArray(1).split(",").map(e => e.toInt.toByte)
          val frade2 = dataArray(2).split(",").map(e => e.toInt.toByte)

          val fullFrade = Array(
            new ArrayIndex[Byte](frade1, 0, frade1.length),
            new ArrayIndex[Byte](frade2, 0, frade2.length)
          )
          val assembled = Main.assemble(fullFrade)
          val decrypted = Main.decryptAndUnshift(assembled)

          fileWriter.append(decrypted.mkString("", ", ", "") + "\n")
//        }


      }

    } finally {
      testTxtSource.close()
    }
  }

  override val tests: Tests = Tests {

    test("real data") - {
      testRealData("/Users/og_pixel/Desktop/1mill.frades", "/Users/og_pixel/Desktop/1mill.frades.decrypted")
    }

    test("test") - {
      val assembled = Main.assemble(garyTestSplit)
      val decrypted = Main.decryptAndUnshift(assembled)
      println("Gary Arr: " + decrypted.mkString("", ", ", ""))
    }

    test("Mixing Test") - {
      assert(
        Main
          .assemble(split)
          .sameElements(merged)
      )
    }

    test("Decrypt and Unshift Test") - {
      val a =
        Main
          .decryptAndUnshift(merged)

      assert(
        a.sameElements(afterMysteryFunction)
      )
    }

    test("Encrypt test") - {
      val a = Main.newEncryptAndShift(afterMysteryFunction)
      println(a._1.mkString("", ", ", ""))
      println(a._1.length)
      println()
      println(merged.mkString("", ", ", ""))
      println(merged.length)

      val b = Main.disassemble(a._1, a._2)

      val c = Main.assemble(b)

      val d = Main.decryptAndUnshift(c)

//      println(afterMysteryFunction.mkString("", ", ", ""))
//      println()
//      println(d.mkString("", ", ", ""))
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

//    test("Encrypt and Split") - {
//      val (a, b) = Main.newEncryptAndShift(SplitData.mergedAndDecrypted)
//
//      val z = Main.disassemble(a, b)
//
//      z.foreach(e => println(e.array.mkString("", ", ", "")))
//    }

//    test("Decrypt ALL types of Events") - {
//      ALL_EVENTS.map {
//        eventFraded =>
//          eventFraded.map { event =>
//            new ArrayIndex[Byte](event, 0, event.length)
//          }
//      }.zipWithIndex.foreach { blockWithIndex =>
//        val block = blockWithIndex._1
//        val index = blockWithIndex._2
//
//        println("len: " + block(0).length)
//        val mixed = Main.assemble(block)
//        val decrypted = Main.decryptAndUnshift(mixed)
//
//        println(s"${decrypted.mkString("", ", ","")}")
//        /////////////////////////////////////////////
//
////        val (encryptedBack, ftotal) = Main.newEncryptAndShift(decrypted)
////        val splitAgain = Main.disassemble(encryptedBack, ftotal)
////
//////        println("len after: " + splitAgain(0).length)
////        /////////////////////////////////////////////
////
////        val mergedAgain = Main.assemble(splitAgain)
////        val decryptedAgain = Main.decryptAndUnshift(mergedAgain)
////
////        println(s"${decryptedAgain.mkString("", ", ","")}")
//      }
//    }

  }

}
