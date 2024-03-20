package com.datasignals.affinetransforms.transformation

import scala.reflect.ClassTag

object GenericBlockTransformation {

  def compose[In, Mid: ClassTag, Out](first: GenericBlockTransformation[In, Mid],
                            mids: Array[GenericBlockTransformation[Mid, Mid]],
                            last: GenericBlockTransformation[Mid, Out]): GenericBlockTransformation[In, Out] =
    composeSeq[In, Mid, Out](first, mids, last)

  def compose[In, Mid: ClassTag, Out](first: GenericBlockTransformation[In, Mid],
                            last: GenericBlockTransformation[Mid, Out]): GenericBlockTransformation[In, Out] =
      compose[In, Mid, Out](first, Array.empty[GenericBlockTransformation[Mid, Mid]], last)

  def outLength(inlength: Int, inBlockSize: Int, outBlockSize: Int): Int = BlockInfo(inlength, inBlockSize, outBlockSize).outLength

  def blockInfo(inlength: Int, inBlockSize: Int, outBlockSize: Int): BlockInfo = BlockInfo(inlength, inBlockSize, outBlockSize)

  private def composeSeq[In, Mid: ClassTag, Out](first: GenericBlockTransformation[In, Mid],
                                       mids: Array[GenericBlockTransformation[Mid, Mid]],
                                       last: GenericBlockTransformation[Mid, Out]): GenericBlockTransformation[In, Out] = {
    var align: Boolean = true
    val inBlockSize: Int = first.inBlockSize
    var outBlockSize: Int = first.outBlockSize
    var max: Int = inBlockSize

    def setAlignAndMax(s: Int): Unit = {
      align &= outBlockSize == s
      if (max < s) max = s
    }

    for (i <- mids.indices) {
      setAlignAndMax(mids(i).inBlockSize)
      outBlockSize = mids(i).outBlockSize
    }

    setAlignAndMax(last.inBlockSize)

    new GenericBlockTransformationComposition[In, Mid, Out](first, mids, last,  max, align)
  }

  private object BlockInfo {
    def apply[In, Out] (length: Int, t: GenericBlockTransformation[In, Out]): BlockInfo = apply(length, t.inBlockSize, t.outBlockSize)

    def apply (length: Int, inBlockSize: Int, outBlockSize: Int): BlockInfo = {
      val k: Int = length % inBlockSize
      val r: Int  = length / inBlockSize
      if(k > 0) {
        val rPlusOne =  r + 1
        new BlockInfo(rPlusOne, rPlusOne * outBlockSize - inBlockSize + k)
      }
      else new BlockInfo(r, r * outBlockSize)
    }

    def apply[In, Mid, Out](length: Int,
              first: GenericBlockTransformation[In, Mid],
              mids: Array[GenericBlockTransformation[Mid, Mid]],
              last: GenericBlockTransformation[Mid, Out]): Array[BlockInfo] = {
      val seq: Array[BlockInfo] = new Array[BlockInfo](mids.length + 2)
      seq(0) = apply(length, first)
      for(i <- mids.indices) seq(i + 1) = apply(seq(i).outLength, mids(i))
      seq(mids.length + 1) = apply(seq(mids.length).outLength, last)
      seq
    }
  }

  final class BlockInfo(val numberOfBlocks: Int, val outLength: Int) {
    override def toString: String = s"BlockInfo(numberOfBlocks = $numberOfBlocks, outLength = $outLength)"
  }

  object GenericBlockTransformationComposition {
    private def maxMidLength(bis: Array[BlockInfo]): Int = {
      var length = bis.head.outLength
      for(i <- 1 until bis.length -1) {
        val l = bis(i).outLength
        if(length < l) length = l
      }
      length
    }
  }

  private class GenericBlockTransformationComposition[In, Mid: ClassTag, Out](
                                                private[this] val first: GenericBlockTransformation[In, Mid],
                                                private[this] val mids: Array[GenericBlockTransformation[Mid, Mid]],
                                                private[this] val last: GenericBlockTransformation[Mid, Out],
                                                private[this] val maxBlockSize: Int,
                                                override val keepsBlockAlignment: Boolean)
    extends GenericBlockTransformation[In, Out] {

    override val inBlockSize: Int = first.inBlockSize
    override val outBlockSize: Int = last.outBlockSize

    override def apply(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int = {

      val bis = BlockInfo(length, first, mids, last)
      if(mids.isEmpty) {
        val array = new Array[Mid](bis.head.outLength)
        first(array, 0, in, inOffset, length)
        last(out, outOffset, array,  0, bis.head.outLength)
      } else {
        val midsLength = mids.length
        val maxMidLength = GenericBlockTransformationComposition.maxMidLength(bis)
        var array0: Array[Mid] = new Array[Mid](maxMidLength)
        var array1: Array[Mid] = new Array[Mid](maxMidLength)
        first(array0, 0, in, inOffset, length)
        for(i <- 0 until midsLength) {
          mids(i)(array1, 0, array0, 0, bis(i).outLength)
          val tmp = array0
          array0 = array1
          array1 = tmp
        }
        last(out, outOffset, array0, 0, bis(midsLength).outLength)
      }
    }

    private val maxMidBlockSize: Int = {
      var length = first.outBlockSize
      for(i <- mids.indices) {
        val l = mids(i).outBlockSize
        if(length < l) length = l
      }
      length
    }

    override def processBlock(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int = {
      if(mids.isEmpty) {
        val array = new Array[Mid](first.outBlockSize)
        val midLength = first.processBlock(array, 0, in, inOffset, length)
        last.processBlock(out, outOffset, array, 0, midLength)
      } else {
        var array0: Array[Mid] = new Array[Mid](maxMidBlockSize)
        var array1: Array[Mid] = new Array[Mid](maxMidBlockSize)
        var midLength = first.processBlock(array0, 0, in, inOffset, length)
        for(i <- mids.indices) {
          midLength = mids(i).processBlock(array1, 0, array0, 0, midLength)
          val tmp = array0
          array0 = array1
          array1 = tmp
        }
        last.processBlock(out, outOffset, array0, 0, midLength)
      }
    }
  }

}

trait GenericBlockTransformation[In, Out] {

  val inBlockSize: Int
  val outBlockSize: Int
  val keepsBlockAlignment: Boolean = true

  /**
   * Applies this transformation to a sequence of blocks in the input array and writes produced blocks into
   * the output array.
   * @param out output array
   * @param outOffset output array offset
   * @param in input array
   * @param inOffset input array offset
   * @param length number of elements to process
   * @return number of processed elements
   */
  def apply(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int = {
    val l = Math.min(length, in.length - inOffset)
    var outOff = outOffset
    var sum = 0
    while(sum < l) {
      sum += processBlock(out, outOff, in, inOffset + sum, inBlockSize)
      outOff += outBlockSize
    }
    sum
  }

  /**
   * Applies this transformation to a single block in the input array and writes produced block into
   * the output array.
   *
   * In most cases (when the block can be processed in full), the method have to return the value
   * of the <code>length</code> parameter. However, implementation must ensure appropriate checks
   * for the <code>length</code> parameter and adjust the return value if necessary to the number
   * of actually processed elements.
   * @param out output array
   * @param outOffset output array offset
   * @param in input array
   * @param inOffset input array offset
   * @param length number of elements to process
   * @return number of processed elements
   */
  def processBlock(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int, length: Int): Int

  def processBlock(out: Array[Out], outOffset: Int, in: Array[In], inOffset: Int): Int = processBlock(out, outOffset, in, inOffset, inBlockSize)

}


