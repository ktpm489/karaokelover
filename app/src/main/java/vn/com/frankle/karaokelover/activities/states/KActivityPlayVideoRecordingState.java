package vn.com.frankle.karaokelover.activities.states;

import vn.com.frankle.karaokelover.activities.KActivityPlayVideo;

/**
 * Created by duclm on 05-Dec-16.
 */

public class KActivityPlayVideoRecordingState extends KActivityPlayVideoBaseState {

    public KActivityPlayVideoRecordingState(KActivityPlayVideo activityPlayVideo) {
        super(activityPlayVideo);
    }

    @Override
    public void setLayout() {
    }

    @Override
    public void onYoutubePlayerInitializedSuccess() {

    }

    @Override
    public void onYoutubePlayerStartPlaying() {

    }

    @Override
    public void onPause() {
        mActivityInstance.stopRecording();
    }

    @Override
    public void onResume() {
        mActivityInstance.reinitializeYoutubePlayerFragment(0);
    }

    @Override
    public void onError() {

    }
}
