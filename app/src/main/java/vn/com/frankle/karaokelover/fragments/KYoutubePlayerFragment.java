package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

/**
 * Created by duclm on 28-Nov-16.
 */

public class KYoutubePlayerFragment extends YouTubePlayerFragment {
    private static final String DEBUG_TAG = KYoutubePlayerFragment.class.getSimpleName();

    public KYoutubePlayerFragment() {
        super();
    }

    public static KYoutubePlayerFragment newInstance() {
        return new KYoutubePlayerFragment();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(DEBUG_TAG, "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d(DEBUG_TAG, "onCreateView");
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(DEBUG_TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(DEBUG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(DEBUG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(DEBUG_TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(DEBUG_TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(DEBUG_TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void initialize(String s, YouTubePlayer.OnInitializedListener onInitializedListener) {
        Log.d(DEBUG_TAG, "initialize");
        super.initialize(s, onInitializedListener);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }
}
