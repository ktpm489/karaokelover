package vn.com.frankle.karaokelover;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.adapters.KSearchRecyclerViewAdapter;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 8/3/2016.
 */

public class KActivityArtistDetails extends AppCompatActivity {


    @BindView(R.id.list_artist_songs)
    RecyclerView mListSongs;
    @BindView(R.id.imgv_backdrop)
    ImageView mArtistAvatar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.progressbar)
    ProgressBar mProgressBar;

    private KSearchRecyclerViewAdapter mSearchAdapter;

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    private final KSearchRecyclerViewAdapter.OnItemClickListener mListener = this::handleOnVideoClickListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViews();
        String artist = getIntent().getExtras().getString("artist");
        mCollapsingToolbar.setTitle(artist);
        loadArtistSongs(artist);
    }

    private void setupViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListSongs.setLayoutManager(layoutManager);
        mListSongs.setHasFixedSize(true);
        mListSongs.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 16), SpaceItemDecoration.VERTICAL));
        mSearchAdapter = new KSearchRecyclerViewAdapter(this, mListener);
        mListSongs.setAdapter(mSearchAdapter);
    }

    private void handleYoutubeResponses(List<VideoSearchItem> songs) {
        mSearchAdapter.appendVideosToList(songs);

        switchLoadingDataState(false);
    }

    private void handleOnVideoClickListener(VideoSearchItem video) {
        Intent playVideoItent = new Intent(this, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", video.getTitle());
        playVideoItent.putExtra("videoid", video.getVideoId());
        startActivity(playVideoItent);
    }

    private void loadArtistSongs(String artist) {

        Observable<List<VideoSearchItem>> obsGetArtistSong =
                ReactiveHelper.searchKarokeVideos(artist);
        compositeSubscriptionForOnStop.add(
                obsGetArtistSong.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleYoutubeResponses));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    /**
     * Switch visiblity of ProgressBar
     *
     * @param loading : true if display progressbar
     */
    private void switchLoadingDataState(boolean loading) {
        if (loading) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
