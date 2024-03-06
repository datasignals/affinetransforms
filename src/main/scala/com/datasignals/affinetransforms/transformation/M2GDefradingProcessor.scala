package com.datasignals.affinetransforms.transformation
//
//import com.datasignals.affinetransforms.entry.{ArrayIndex, Bits}
//import com.datasignals.affinetransforms.entry.Bits.LOG_LONG_BYTES
//import com.datasignals.affinetransforms.string.ImplicitHexStringByteArray
//import com.typesafe.scalalogging.StrictLogging
//
//import java.lang.Integer.{BYTES => INT_BYTES}
//import scala.concurrent.Future
//import scala.util.control.NonFatal
//
//class M2GDefradingProcessor(mixer: Mixer[ArrayIndex[Byte]], t: DecryptAndUnshift/*GenericArrayEncoder[Byte, Byte]*/) {
//
//  private[this] val dim = mixer.dimension
//  private[this] val d = dim << LOG_LONG_BYTES
//
//  def apply(input: Array[ArrayIndex[Byte]]/*Record[M2GEventKey, Array[ArrayIndex[Byte]]]*/): Array[Byte] = {
//      val inputValue = input.value
//
//      val ftotal = inputValue(0).length
//      val total = dim * ftotal
//
//      val vArray = new Array[Byte](total)
////      val vIndex = new ArrayIndex[Byte](vArray, 0)
////      mixer(vIndex, inputValue)
//
//
//      val valueArray = t(vArray)
//
//  }
//}
