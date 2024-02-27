package datasignals.transform.matrix.jni;

public class JNativeMatrixSplitter {

    native public static void split(byte[][] out, int[] outPositions,
                             long[] matrix, int dimension,
                             byte[] in, int inPosition, int inLength);

    native public static void concurrentSplit(byte[][] out, int[] outPositions,
                             long[] matrix, int dimension,
                             byte[] in, int inPosition, int inLength,
                             int numberOfThreads);
}
