package vn.com.frankle.karaokelover.activities.states;

import vn.com.frankle.karaokelover.KAudioRecord;

/**
 * Created by duclm on 03-Jan-17.
 */

public abstract class KAudioRecordBaseState {

    protected KAudioRecord mAudioRecordInstance;

    public KAudioRecordBaseState(KAudioRecord audioRecordInst) {
        this.mAudioRecordInstance = audioRecordInst;
    }

    public abstract Runnable getAudioRecordRunnable(String recordFilename, boolean isResume);

    public abstract void stopRecording();
}
