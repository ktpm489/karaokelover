package vn.com.frankle.karaokelover;

import android.util.Log;

import com.cleveroad.audiovisualization.DbmHandler;

import vn.com.frankle.karaokelover.util.AudioChunk;

/**
 * Created by duclm on 8/27/2016.
 */
public class KAudioRecordDbmHandler extends DbmHandler<Float> implements KAudioRecord.AudioRecordListener {


    @Override
    public void onAudioRecordDataReceived(byte[] data) {
        onDataReceived((float) AudioChunk.getMaxAmplitude(data));
    }

    @Override
    public void onError() {

    }

    public void stopVisualizer() {
        calmDownAndStopRendering();
    }

    @Override
    protected void onDataReceivedImpl(Float amplitude, int layersCount, float[] dBmArray, float[] ampsArray) {
        Log.d("visualize", "onDataReceivedImpl: amplitude" + amplitude);
        amplitude = amplitude / 100;
        if (amplitude <= 0.5) {
            amplitude = 0.0f;
        } else if (amplitude > 0.5 && amplitude <= 0.6) {
            amplitude = 0.2f;
        } else if (amplitude > 0.6 && amplitude <= 0.7) {
            amplitude = 0.6f;
        } else if (amplitude > 0.7) {
            amplitude = 1f;
        }
        try {
            dBmArray[0] = amplitude;
            ampsArray[0] = amplitude;
        } catch (Exception e) {
        }
    }
}
