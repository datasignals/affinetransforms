package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.ArrayIndex
import com.github.sbt.jni.nativeLoader
import datasignals.transform.matrix.jni.JNativeMatrixSplitter

@nativeLoader("msplit0")
object NativeMatrixSplitter {

  @inline private def split(out: Array[ArrayIndex[Byte]], matrix: Array[Long], dimension: Int, in: ArrayIndex[Byte]): Unit = {
    val positions = new Array[Int](dimension)
    val arrays = new Array[Array[Byte]](dimension)
    extractArrayIndices(arrays, positions, out, dimension)

    JNativeMatrixSplitter.split(arrays, positions, matrix, dimension, in.array, in.position, in.length)
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

class NativeMatrixSplitter(override val dimension: Int, private[this] val matrix: Array[Long])
  extends Splitter[ArrayIndex[Byte]] {

  @inline override def apply(out: Array[ArrayIndex[Byte]], in: ArrayIndex[Byte]): Unit =
    NativeMatrixSplitter.split(out, matrix, dimension, in)

}
