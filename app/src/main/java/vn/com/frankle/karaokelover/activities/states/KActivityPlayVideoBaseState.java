package vn.com.frankle.karaokelover.activities.states;

import vn.com.frankle.karaokelover.activities.KActivityPlayVideo;

/**
 * Created by duclm on 05-Dec-16.
 */

public abstract class KActivityPlayVideoBaseState {

    KActivityPlayVideo mActivityInstance;

    KActivityPlayVideoBaseState(KActivityPlayVideo activityPlayVideo) {
        this.mActivityInstance = activityPlayVideo;
    }

    public abstract void setLayout();

    public abstract void onYoutubePlayerInitializedSuccess();

    public abstract void onYoutubePlayerStartPlaying();

    public abstract void onPause();

    public abstract void onResume();

    public abstract void onError();
}
