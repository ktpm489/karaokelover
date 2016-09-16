package vn.com.frankle.karaokelover.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by duclm on 8/27/2016.
 */

public class AudioChunk {
    private static final double REFERENCE = 0.6;

    public static double getMaxAmplitude(byte[] data) {
        short[] shorts = toShorts(data);
        int nMaxAmp = 0;
        int arrLen = shorts.length;
        int peakIndex;
        for (peakIndex = 0; peakIndex < arrLen; peakIndex++) {
            if (shorts[peakIndex] >= nMaxAmp) {
                nMaxAmp = shorts[peakIndex];
            }
        }
        return (int) (20 * Math.log10(nMaxAmp / REFERENCE));
    }

    public static short[] toShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
}
