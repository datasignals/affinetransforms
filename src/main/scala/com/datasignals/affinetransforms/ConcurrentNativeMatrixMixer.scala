package com.datasignals.affinetransforms

//import ch.jodersky.jni.nativeLoader
import com.datasignals.affinetransforms.entry.ArrayIndex
import com.github.sbt.jni.nativeLoader
import datasignals.transform.matrix.jni.JNativeMatrixMixer

@nativeLoader("msplit0")
object ConcurrentNativeMatrixMixer {

  @inline private def mix(out: ArrayIndex[Byte], matrix: Array[Long], dimension: Int, in: Array[ArrayIndex[Byte]],
                            nThreads: Int): Unit = {
    val positions = new Array[Int](dimension)
    val arrays = new Array[Array[Byte]](dimension)
    extractArrayIndices(arrays, positions, in, dimension)
    JNativeMatrixMixer.concurrentMix(out.array, out.position, matrix, dimension, arrays, positions, in(0).length, nThreads)
  }

  @inline private def extractArrayIndices(arrays: Array[Array[Byte]], positions: Array[Int],
                                               indices: Array[ArrayIndex[Byte]], dimension: Int): Unit = {
    var i = 0
    do {
      arrays(i) = indices(i).array
      positions(i) = indices(i).position
      i += 1
    } while(i < dimension)
  }

}

class ConcurrentNativeMatrixMixer(override val dimension: Int, private[this] val matrix: Array[Long],
                                  private[this] val nThreads: Int)
  extends Mixer[ArrayIndex[Byte]] {

  @inline override def apply(out: ArrayIndex[Byte], in: Array[ArrayIndex[Byte]]): Unit =
    ConcurrentNativeMatrixMixer.mix(out, matrix, dimension, in, nThreads)

}
