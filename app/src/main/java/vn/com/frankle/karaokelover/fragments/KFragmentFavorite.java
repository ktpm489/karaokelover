package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KActivityPlayVideo;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.KAdapterYoutbeVideoSearchLimit;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.KSharedPreference;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentFavorite extends Fragment {

    public static final String TAG = "FRAGMENT_FAVORITE";
    public static final int REQUEST_CODE_RELOAD_FAVORITE_LIST = 111;
    private static final String DEBUG_TAG = KFragmentFavorite.class.getSimpleName();
    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.progressbar_favorite)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_my_favorite)
    RecyclerView mRecyclerView;
    private Context mContext;
    private KSharedPreference mAppPrefs = new KSharedPreference();
    private int mCurSizeList = 0;
    private KSharedPreference sharePrefs = new KSharedPreference();
    private KAdapterYoutbeVideoSearchLimit mFavoriteAdapter;

    public KFragmentFavorite() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "onStop");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "onSaveInstanceState");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(DEBUG_TAG, "onViewStateRestored");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(DEBUG_TAG, "onActivityCreated");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreateView");

        View layout = inflater.inflate(R.layout.layout_fragment_my_favorite, container, false);

        ButterKnife.bind(this, layout);

        setupFavoriteView();

        // Load list of favorite video
        ArrayList<String> listFavoriteId = sharePrefs.getFavoritesVideo(mContext);
        loadFavoriteVideos(listFavoriteId);

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(DEBUG_TAG, "onActivityResult");
        switch (requestCode) {
            case KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST:
                reloadIfNecessary(data);
                break;
        }
    }

    /**
     * Setup view for displaying search result
     */
    private void setupFavoriteView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 16), SpaceItemDecoration.VERTICAL));
        mFavoriteAdapter = new KAdapterYoutbeVideoSearchLimit(mContext);
        Observable<VideoSearchItem> clickListenerObservable = mFavoriteAdapter.getItemClickListener();
        compositeSubscriptionForOnStop.add(clickListenerObservable.subscribe(this::handleOnVideoClickListener));
        mRecyclerView.setAdapter(mFavoriteAdapter);
    }

    private void loadFavoriteVideos(ArrayList<String> listFavoriteId) {
        setLoadingState(true);
        mCurSizeList = listFavoriteId.size();

        Observable<List<VideoSearchItem>> favoriteRequest = ReactiveHelper.getFavoritesVideos(listFavoriteId);
        compositeSubscriptionForOnStop.add(favoriteRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleFavoriteVideoList));
    }

    private void handleFavoriteVideoList(List<VideoSearchItem> favoriteVideos) {

        if (favoriteVideos.size() > 0) {
            setLoadingState(false);

            mFavoriteAdapter.addDataItemList(favoriteVideos);
        } else {
            Toast.makeText(mContext, "Empty favorite videos list", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOnVideoClickListener(VideoSearchItem video) {
        Intent playVideoItent = new Intent(mContext, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", video.getTitle());
        playVideoItent.putExtra("videoid", video.getVideoId());
        playVideoItent.putExtra("from_favorite", true);
        startActivityForResult(playVideoItent, REQUEST_CODE_RELOAD_FAVORITE_LIST);
    }

    /**
     * Reload the favorite video list if necessary (change from other activity)
     *
     * @param resultData : intent data sent by called activity
     */
    public void reloadIfNecessary(Intent resultData) {
        if (resultData != null) {
            String videoId = resultData.getStringExtra("video_id");
            if (mAppPrefs.isInFavoriteList(mContext, videoId)) {
                // User has add this video to the favorite list
                ArrayList<String> newVideo = new ArrayList<>();
                newVideo.add(videoId);
                loadFavoriteVideos(newVideo);
            } else {
                // User remove this video from the favorite list
                mFavoriteAdapter.removeVideoFromList(videoId);
            }
        }
    }

    /**
     * Switch visiblity of ProgressBar and RecyclerView
     *
     * @param loading : true if display progressbar
     */
    private void setLoadingState(boolean loading) {
        if (loading) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
