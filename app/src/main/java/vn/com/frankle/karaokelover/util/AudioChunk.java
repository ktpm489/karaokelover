package vn.com.frankle.karaokelover.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by duclm on 8/27/2016.
 */

public class AudioChunk {
    private static final double REFERENCE = 0.6;

    public static float getMaxAmplitude(byte[] data, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += data[i] * data[i];
        }
        if (readSize > 0) {
            return (float) sum / readSize;
        }
        return 0;
    }

    public static short[] toShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
}
