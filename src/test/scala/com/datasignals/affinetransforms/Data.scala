package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.Main.{inputValue1, inputValue2}
import com.datasignals.affinetransforms.entry.ArrayIndex

trait Data {

  val inputArray: Array[Byte] = Array(
    42, 73, -98, -65, 32, -115, 44, 124, 107, 109, -75, -8, 55, 116, 120, -75,
    -10, -80, 67, 1, 109, 8, 74, 102, -58, -12, 51, 36, 77, 62, 38, 64, -33,
    102, 50, 16, -35, 7, 21, 126, 95, -123, -29, -72, -54, -95, 72, 106, -6, 17,
    -9, -22, -41, 17, -14, -104, -89, 124, -84, 87, 40, 50, 107, -72, 112, -29,
    -56, -31, 75, 27, 5, 66, -26, 40, 68, 29, 36, -58, 27, 58, 2, -83, -99, 105,
    92, -85, -26, 18, -99, -69, 20, -64, 76, 110, 101, -46, -102, 23, -95, 65,
    -116, -93, 12, 82, 101, -109, -128, 70, 63, -39, 67, -28
  )

  val resultArray: Array[Byte] = Array(
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
    -60, -46, 31, -75, 42, 15, -79, -32, 111, 19, -82, 28, 15, 24, -122, -100,
    103, 122, -125, 120, 60, -2, 57, -53, -65, 87, 125, -62, 34, -56, 66, 108,
    -38, -127, -32, -99, 67, 60, 93, -100, -75, 126, -6, -58, -69, -74, -33,
    -26, 100, 104, 4, 75, -74, -97, -36, -25
  )

  //ArrayIndex used for mixing, it represents a message split two ways
  val arrayIndex: Array[ArrayIndex[Byte]] = Array(
    new ArrayIndex[Byte](inputValue1, 0, inputValue1.length),
    new ArrayIndex[Byte](inputValue2, 0, inputValue2.length),
  )

}
