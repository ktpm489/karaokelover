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

    private Context mContext;

    @BindView(R.id.progressbar_favorite)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_my_favorite)
    RecyclerView mRecyclerView;

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
    public void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.layout_fragment_my_favorite, container, false);

        ButterKnife.bind(this, layout);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Favorites");

        setupFavoriteView();

        loadFavoriteVideos();

        return layout;
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

    private void loadFavoriteVideos() {
        ArrayList<String> listFavoriteId = sharePrefs.getFavoritesVideo(mContext);

        Observable<List<VideoSearchItem>> favoriteRequest = ReactiveHelper.getFavoritesVideos(listFavoriteId);
        compositeSubscriptionForOnStop.add(favoriteRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleFavoriteVideoList));
    }

    private void handleFavoriteVideoList(List<VideoSearchItem> favoriteVideos) {

        if (favoriteVideos.size() > 0) {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mFavoriteAdapter.populateWithData(favoriteVideos);
        } else {
            Toast.makeText(mContext, "Empty favorite videos list", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOnVideoClickListener(VideoSearchItem video) {
        Intent playVideoItent = new Intent(mContext, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", video.getTitle());
        playVideoItent.putExtra("videoid", video.getVideoId());
        startActivity(playVideoItent);
    }
}
