package com.datasignals.affinetransforms.transformation.helpers

import java.util.concurrent.{Callable, ExecutorService, Future}
import scala.reflect.ClassTag

object GenericConcurrentThreadPoolTransformation {

  val BlocksPerThread: Int = 1 << 16 //65536

  private class BlockCallable[In, Out](private[this] val out: Array[Out],
                              private[this] val outOffset: Int,
                              private[this] val in: Array[In],
                              private[this] val inOffset: Int,
                              private[this] val length: Int,
                              private[this] val t: GenericBlockTransformation[In, Out])
    extends Callable[Unit] {
    override def call(): Unit = t.apply(out, outOffset, in, inOffset, length)
  }

}

class GenericConcurrentThreadPoolTransformation[In: ClassTag, Out: ClassTag](private[this] val threadPool: ExecutorService,
                                                                             private[this] val blocksPerThread: Int =
                                                                      GenericConcurrentThreadPoolTransformation.BlocksPerThread)
                                                                            (private[this] val encode: GenericBlockTransformation[In, Out],
                                               private[this] val decode: GenericBlockTransformation[Out, In])
  extends GenericArrayTransformation[In, Out] {

  @inline def this(threadPool: ExecutorService,
                   blocksPerThread: Int,
                   wrapper: GenericBlockWrapper[In, Out]) =
    this(threadPool, blocksPerThread)(wrapper.getWrapper, wrapper.getUnwrapper)

  @inline def this(threadPool: ExecutorService,
                   wrapper: GenericBlockWrapper[In, Out]) =
    this(threadPool)(wrapper.getWrapper, wrapper.getUnwrapper)

  override val isBlockPreserving: Boolean = encode.inBlockSize == decode.outBlockSize

  @inline private def nFutures(nBlocks: Int): Int = {
    val k = nBlocks / blocksPerThread
    val r = nBlocks % blocksPerThread
    if(r > 0) k + 1 else k
  }

  @inline private def execute[I, O: ClassTag](data: Array[I], t: GenericBlockTransformation[I, O]): Array[O] = {
    val inBlockSize = t.inBlockSize
    val outBlockSize = t.outBlockSize
    val info = GenericBlockTransformation.blockInfo(data.length, inBlockSize, outBlockSize)
    val nBlocks = info.numberOfBlocks
    val out = new Array[O](info.outLength)
    val fLength = nFutures(nBlocks)
    val futures = new Array[Future[Unit]](fLength)
    var outOffset = 0
    var inOffset = 0
    val inLength = blocksPerThread * inBlockSize
    val outLength = blocksPerThread * outBlockSize
    var i = 0
    while (i < fLength) {
      //System.err.println(s"Out length: ${out.length}, out offset: $outOffset, data length: ${data.length}, in offset: $inOffset, inLength: $inLength")
      futures(i) = threadPool.submit[Unit](
        new GenericConcurrentThreadPoolTransformation.BlockCallable[I, O](out, outOffset, data, inOffset, inLength, t))
      outOffset += outLength
      inOffset += inLength
      i += 1
    }
    i = 0
    while (i < fLength) {
      futures(i).get()
      i += 1
    }
    out
  }

  override def encode(data: Array[In]): Array[Out] = execute[In, Out](data, encode)

  override def decode(data: Array[Out]): Array[In] = execute[Out, In](data, decode)
}
