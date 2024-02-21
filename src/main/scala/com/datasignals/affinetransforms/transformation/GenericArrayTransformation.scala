package com.datasignals.affinetransforms.transformation

trait GenericArrayTransformation[In, Out] {

  val isBlockPreserving: Boolean

  def encode(data: Array[In]): Array[Out]

  def decode(data: Array[Out]): Array[In]

}
