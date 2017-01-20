package vn.com.frankle.karaokelover.activities.states;

import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;

import vn.com.frankle.karaokelover.activities.KActivityPlayVideo;

/**
 * Created by duclm on 05-Dec-16.
 */

public class KActivityPlayVideoPlayingState extends KActivityPlayVideoBaseState {

    private static final String DEBUG_TAG = KActivityPlayVideoPlayingState.class.getSimpleName();

    private int mCurrentVideoPos;

    public KActivityPlayVideoPlayingState(KActivityPlayVideo activityPlayVideo) {
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
        Log.d(DEBUG_TAG, "onPause");
        YouTubePlayer youTubePlayer = mActivityInstance.getYoutubePlayerInstance();
        if (youTubePlayer != null) {
            if (youTubePlayer.isPlaying()) {
                try {
                    youTubePlayer.pause();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            mCurrentVideoPos = youTubePlayer.getCurrentTimeMillis();
        }

        mActivityInstance.cancelPrepareRecordingTask();
    }

    @Override
    public void onResume() {
        Log.d(DEBUG_TAG, "onResume");
        YouTubePlayer youTubePlayer = mActivityInstance.getYoutubePlayerInstance();
        if (youTubePlayer != null) {
            //Setup youtube player view
            mActivityInstance.reinitializeYoutubePlayerFragment(mCurrentVideoPos);
        }
    }

    @Override
    public void onError() {

    }
}
