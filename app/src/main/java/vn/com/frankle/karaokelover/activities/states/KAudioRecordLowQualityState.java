package vn.com.frankle.karaokelover.activities.states;

import android.media.MediaRecorder;

import java.io.IOException;

import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.KAudioRecord;

/**
 * Created by duclm on 03-Jan-17.
 */

public class KAudioRecordLowQualityState extends KAudioRecordBaseState {

    private MediaRecorder mMediaRecorder;

    public KAudioRecordLowQualityState(KAudioRecord audioRecordInst) {
        super(audioRecordInst);
    }


    @Override
    public Runnable getAudioRecordRunnable(String recordFilename, boolean isResume) {
        return new LowQualityAudioRecordRunnable(recordFilename, isResume);
    }

    @Override
    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private class LowQualityAudioRecordRunnable implements Runnable {
        private String mFilename;
        private boolean mIsResume;

        public LowQualityAudioRecordRunnable(String filename, boolean isResume) {
            this.mFilename = KApplication.Companion.getRECORDING_DIRECTORY_URI() + filename;
            this.mIsResume = isResume;
        }

        @Override
        public void run() {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(mFilename);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                mAudioRecordInstance.getAudioRecordListener().onAudioRecordError();
            }
            mMediaRecorder.start();
        }
    }

}
