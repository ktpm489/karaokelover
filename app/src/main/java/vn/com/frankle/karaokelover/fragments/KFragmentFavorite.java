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
import vn.com.frankle.karaokelover.adapters.KSearchRecyclerViewAdapter;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.util.KSharedPreference;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentFavorite extends Fragment {

    private static final String DEBUG_TAG = KFragmentFavorite.class.getSimpleName();
    public static final String TAG = "FRAGMENT_FAVORITE";

    private static final int REQUEST_CODE_RELOAD_FAVORITE_LIST = 111;

    private Context mContext;

    @BindView(R.id.progressbar_favorite)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_my_favorite)
    RecyclerView mRecyclerView;

    private int mCurSizeList = 0;

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    private KSharedPreference sharePrefs = new KSharedPreference();
    private KSearchRecyclerViewAdapter mFavoriteAdapter;

    private final KSearchRecyclerViewAdapter.OnItemClickListener mListener = this::handleOnVideoClickListener;

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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Favorites");

        setupFavoriteView();

        // Load list of favorite video
        ArrayList<String> listFavoriteId = sharePrefs.getFavoritesVideo(mContext);
        loadFavoriteVideos(listFavoriteId);

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(DEBUG_TAG, "onActivityResult");
        switch (requestCode) {
            case KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST:
                reloadIfNecessary();
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
        mFavoriteAdapter = new KSearchRecyclerViewAdapter(mContext, mListener);
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

            mFavoriteAdapter.populateWithData(favoriteVideos);
        } else {
            Toast.makeText(mContext, "Empty favorite videos list", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOnVideoClickListener(VideoSearchItem video) {
        Intent playVideoItent = new Intent(mContext, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", video.getTitle());
        playVideoItent.putExtra("videoid", video.getVideoId());
        startActivityForResult(playVideoItent, REQUEST_CODE_RELOAD_FAVORITE_LIST);
    }

    /**
     * Reload the favorite video list if necessary (change from other activity)
     */
    private void reloadIfNecessary() {
        ArrayList<String> listFavoriteId = sharePrefs.getFavoritesVideo(mContext);
        if (mCurSizeList != listFavoriteId.size()) {
            loadFavoriteVideos(listFavoriteId);
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
