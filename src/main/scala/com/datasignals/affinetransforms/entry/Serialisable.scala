package com.datasignals.affinetransforms.entry

trait Serialisable[T <: Serialisable[T]] {

  /**
   * Serialises t into byte array from specified offset.
   *
   * @param out output byte array
   * @param offset offset in the output array
   * @return number of serialised bytes
   */
  def serialise(out: Array[Byte], offset: Int): Int
//  val serialisationInfo: SerialisationInfo[T]
//
//  //convenience methods
//  lazy val serialisationLength: Int = serialisationInfo.length(this.asInstanceOf[T])
//
//  def serialise(): Array[Byte] = {
//    val res = new Array[Byte](serialisationLength)
//    serialise(res, 0)
//    res
//  }
}
