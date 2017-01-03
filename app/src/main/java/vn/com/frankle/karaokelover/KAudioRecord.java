package vn.com.frankle.karaokelover;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import vn.com.frankle.karaokelover.activities.KActivitySettings;
import vn.com.frankle.karaokelover.activities.states.KAudioRecordBaseState;
import vn.com.frankle.karaokelover.activities.states.KAudioRecordHighQualityState;
import vn.com.frankle.karaokelover.activities.states.KAudioRecordLowQualityState;

/**
 * Created by duclm on 8/8/2016.
 */

public class KAudioRecord {
    private static String DEBUG_TAG = KAudioRecord.class.getSimpleName();

    public interface AudioRecordListener {
        @WorkerThread
        void onAudioRecordDataReceived(byte[] data, int readSize);

        void onAudioRecordError();
    }

    private Context mContext;

    private KAudioRecordBaseState mRecordState;
    private final KAudioRecordHighQualityState mHighQualityRecordState = new KAudioRecordHighQualityState(this);
    private final KAudioRecordLowQualityState mLowQualityRecordState = new KAudioRecordLowQualityState(this);

    private AudioRecordListener mDataListener;
    private final AtomicBoolean mIsRecording;
    private ExecutorService mExecutorService;

    public KAudioRecord(Context context, @NonNull AudioRecordListener dataListener) {
        this.mContext = context;
        mDataListener = dataListener;
        mIsRecording = new AtomicBoolean(false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isHighQualityRecord = prefs.getBoolean(KActivitySettings.SettingsFragment.KEY_PREF_HD_RECORD, false);

        if (isHighQualityRecord) {
            mRecordState = mHighQualityRecordState;
        } else {
            mRecordState = mLowQualityRecordState;
        }
    }

    public boolean isRecording() {
        return mIsRecording.get();
    }

    public AudioRecordListener getAudioRecordListener() {
        return mDataListener;
    }

    public KAudioRecordBaseState getAudioRecordState() {
        return mRecordState;
    }

    /**
     * Start recording voice
     *
     * @param recordFilename : the name of the file to be saved
     * @param isResume       : flag to indicate this record is resume from previous time or not
     * @return
     */
    public synchronized boolean start(String recordFilename, boolean isResume) {
        stop();

        mExecutorService = Executors.newSingleThreadExecutor();

        if (mIsRecording.compareAndSet(false, true)) {
            mExecutorService.execute(mRecordState.getAudioRecordRunnable(recordFilename, isResume));
            return true;
        }
        return false;
    }

    public synchronized void stop() {
        mIsRecording.compareAndSet(true, false);

        if (mExecutorService != null) {
            mRecordState.stopRecording();
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }
}
