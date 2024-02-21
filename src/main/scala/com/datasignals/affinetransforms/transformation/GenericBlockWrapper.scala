package com.datasignals.affinetransforms.transformation

trait GenericBlockWrapper[In, Out] {

  val wrapBlockSize: Int
  val unwrapBlockSize: Int

  def wrap(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int
  def unwrap(out: Array[In], outOffset: Int, in: Array[Out], inOffset: Int, length: Int): Int

  def getWrapper: GenericBlockTransformation[In, Out] =
    new GenericBlockTransformation[In, Out] {
      override val inBlockSize: Int = wrapBlockSize
      override val outBlockSize: Int = unwrapBlockSize

      override def processBlock(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int =
        wrap(out, outOffset, in, inOffset, length)
    }

  def getUnwrapper: GenericBlockTransformation[Out, In] =
    new GenericBlockTransformation[Out, In] {
      override val inBlockSize: Int = unwrapBlockSize
      override val outBlockSize: Int = wrapBlockSize

      override def processBlock(out: Array[In], outOffset: Int, in: Array[Out], inOffset: Int, length: Int): Int =
        unwrap(out, outOffset, in, inOffset, length)
    }

}
