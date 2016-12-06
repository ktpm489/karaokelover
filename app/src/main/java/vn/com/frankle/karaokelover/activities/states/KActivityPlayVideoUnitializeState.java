package vn.com.frankle.karaokelover.activities.states;

import vn.com.frankle.karaokelover.activities.KActivityPlayVideo;

/**
 * Created by duclm on 05-Dec-16.
 */

public class KActivityPlayVideoUnitializeState extends KActivityPlayVideoBaseState {

    public KActivityPlayVideoUnitializeState(KActivityPlayVideo activityPlayVideo) {
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
        mActivityInstance.setActivityState(mActivityInstance.getPlayingState());
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onError() {

    }
}
