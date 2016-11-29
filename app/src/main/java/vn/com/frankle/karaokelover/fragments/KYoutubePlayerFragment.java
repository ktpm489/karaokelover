package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Created by duclm on 28-Nov-16.
 */

public class KYoutubePlayerFragment extends YouTubePlayerSupportFragment {
    private static final String DEBUG_TAG = KYoutubePlayerFragment.class.getSimpleName();

    public KYoutubePlayerFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(DEBUG_TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(DEBUG_TAG, "onAttach");
    }

    @Override
    public void initialize(String s, YouTubePlayer.OnInitializedListener onInitializedListener) {
        Log.d(DEBUG_TAG, "initialize");
        super.initialize(s, onInitializedListener);
    }
}
