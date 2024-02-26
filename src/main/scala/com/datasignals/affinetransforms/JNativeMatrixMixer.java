package com.datasignals.affinetransforms;

class JNativeMatrixMixer {

    native static void mix(byte[] out, int outPosition,
                             long[] matrix, int dimension,
                             byte[][] in, int[] inPositions, int inLength);

    native static void concurrentMix(byte[] out, int outPosition,
                             long[] matrix, int dimension,
                             byte[][] in, int[] inPositions, int inLength,
                             int numberOfThreads);
}
