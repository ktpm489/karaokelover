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
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterYoutubeVideoSearch;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 8/3/2016.
 */

public class KActivityArtistDetails extends AppCompatActivity {


    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
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
    private KAdapterYoutubeVideoSearch mVideoSearchAdapter;

    private String mArtistName;
    private String mNextPageToken;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViews();
        mArtistName = getIntent().getExtras().getString("artist");
        mCollapsingToolbar.setTitle(mArtistName);
        loadArtistSongs();
    }

    private void setupViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListSongs.setLayoutManager(layoutManager);
        mListSongs.setHasFixedSize(true);
        mListSongs.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 16), SpaceItemDecoration.VERTICAL));
        mVideoSearchAdapter = new KAdapterYoutubeVideoSearch(this);
        Observable<VideoSearchItem> itemClickObservable = mVideoSearchAdapter.onItemClickListener();
        compositeSubscriptionForOnStop.add(itemClickObservable.subscribe(this::handleVideoItemClick));
        mListSongs.setAdapter(mVideoSearchAdapter);
        mListSongs.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                loadMoreArtistSongs();
            }
        });
    }

    private void handleVideoItemClick(VideoSearchItem clickedVideoItem) {
        Intent playVideoItent = new Intent(this, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", clickedVideoItem.getTitle());
        playVideoItent.putExtra("videoid", clickedVideoItem.getVideoId());
        startActivity(playVideoItent);
    }

    private void handleYoutubeResponses(List<VideoSearchItem> songs) {
        mVideoSearchAdapter.addDataItems(songs);
        switchLoadingDataState(false);
    }

    private void handleLoadMoreResultResponses(List<VideoSearchItem> songs) {
        mVideoSearchAdapter.addDataItems(songs);
        switchLoadingDataState(false);
    }

    private void loadArtistSongs() {

        String karaokeQuery = mArtistName + " karaoke";

        Observable<List<VideoSearchItem>> obsGetArtistSong = KApplication.getRxYoutubeAPIService()
                .searchKaraokeVideos(karaokeQuery)
                .concatMap(
                        responseSearch -> {
                            mNextPageToken = responseSearch.getNextPageToken();
                            return Observable.from(responseSearch.getItems())
                                    .subscribeOn(Schedulers.newThread())
                                    .concatMap(ReactiveHelper::getStatisticsContentDetails);
                        })
                .toList();
        compositeSubscriptionForOnStop.add(
                obsGetArtistSong.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleYoutubeResponses));

    }

    private void loadMoreArtistSongs() {
        String karaokeQuery = mArtistName + " karaoke";

        Observable<List<VideoSearchItem>> obsLoadMoreRequest = KApplication.getRxYoutubeAPIService()
                .searchYoutubeVideoNext(karaokeQuery, mNextPageToken)
                .concatMap(
                        responseSearch -> {
                            mNextPageToken = responseSearch.getNextPageToken();
                            return Observable.from(responseSearch.getItems())
                                    .subscribeOn(Schedulers.newThread())
                                    .concatMap(ReactiveHelper::getStatisticsContentDetails);
                        })
                .toList();
        compositeSubscriptionForOnStop.add(obsLoadMoreRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleLoadMoreResultResponses));
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
