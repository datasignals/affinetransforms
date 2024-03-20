package datasignals.transform.matrix.jni;

public class JNativeMatrixMixer {

    native public static void mix(byte[] out, int outPosition,
                             long[] matrix, int dimension,
                             byte[][] in, int[] inPositions, int inLength);

    native public static void concurrentMix(byte[] out, int outPosition,
                             long[] matrix, int dimension,
                             byte[][] in, int[] inPositions, int inLength,
                             int numberOfThreads);
}
