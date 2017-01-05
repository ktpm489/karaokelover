package vn.com.frankle.karaokelover.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.KSharedPreference;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterYoutubeVideoSearch;
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm;
import vn.com.frankle.karaokelover.events.EventPopupMenuItemClick;
import vn.com.frankle.karaokelover.services.responses.youtube.playlist.ResponsePlaylist;
import vn.com.frankle.karaokelover.services.responses.youtube.playlist.Snippet;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

public class KActivityPlaylist extends AppCompatActivity {

    private static final String DEBUG_TAG = KActivityPlaylist.class.getSimpleName();
    public static final String EXTRA_PLAYLIST_ID = "playlist_id";
    public static final String EXTRA_TITLE = "playlist_title";

    @BindView(R.id.playlist_video_number)
    TextView mVideoNumber;
    @BindView(R.id.playlist_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.playlist_progressbar)
    ProgressBar mProgressBar;
    @BindView(R.id.layout_connection_error)
    RelativeLayout mLayoutConnectionError;
    @BindView(R.id.content_error_loading)
    RelativeLayout mLayoutContentErrorLoading;
    @BindView(R.id.layout_playlist_content)
    RelativeLayout mLayoutMainContent;

    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    private KAdapterYoutubeVideoSearch adapter;
    private int mTotalVideoNumber = 0;
    private String mNextPageTokern;

    private String mPlaylistId;
    private String mPlaylistTitle;
    private EndlessRecyclerViewScrollListener OnLoadMoreListener;

    private Realm realm;
    // This set is used to store video id that change its favorite state
    // When the set is empty, it means that the favorite video list remains unchanged
    private Set<String> mFavoriteStateSet = new HashSet<>();
    private KSharedPreference mSharedPrefs = new KSharedPreference(this);

    @Override
    protected void onStart() {
        super.onStart();
        KApplication.eventBus.register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // Get intent extra
        mPlaylistId = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);
        mPlaylistTitle = getIntent().getStringExtra(EXTRA_TITLE);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mPlaylistTitle);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLayoutConnectionError.setOnClickListener(view -> {
            checkInternetConnectionAndInitilaizeViews();
        });

        checkInternetConnectionAndInitilaizeViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If favorite state set is not empty, it means that the favorite video list has been changed
        if (!mFavoriteStateSet.isEmpty()) {
            mSharedPrefs.setFavoriteListReloadFlag(this, true);
            // We updated favorite reload flag in SharedPreference, so clear the set
            mFavoriteStateSet.clear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        KApplication.eventBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscriptionForOnStop.unsubscribe();
    }

    private void setupViews() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 0), SpaceItemDecoration.VERTICAL));
        adapter = new KAdapterYoutubeVideoSearch(this, new RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem>() {
            @Override
            public void onDataItemClick(VideoSearchItem dataItem) {
                Intent playVideoItent = new Intent(KActivityPlaylist.this, KActivityPlayVideo.class);
                playVideoItent.putExtra("title", dataItem.getTitle());
                playVideoItent.putExtra("videoid", dataItem.getVideoId());
                startActivity(playVideoItent);
            }

            @Override
            public void onErrorLoadMoreRetry() {
                adapter.setErrorLoadingMore(false);
                loadPlaylistVideosNext();
            }
        });
        mRecyclerView.setAdapter(adapter);
        OnLoadMoreListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                loadPlaylistVideosNext();
            }
        };
        mRecyclerView.addOnScrollListener(OnLoadMoreListener);
    }

    private void checkInternetConnectionAndInitilaizeViews() {
        if (Utils.isOnline(this)) {
            setupViews();
            loadPlaylistVideos();
        } else {
            setLayoutContentType(LayoutContent.ERROR_CONNECTION);
        }
    }

    private void loadPlaylistVideos() {
        setLayoutContentType(LayoutContent.LOADING);
        compositeSubscriptionForOnStop.add(getPlaylistItemObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoSearchItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        setLayoutContentType(LayoutContent.ERROR_CONNECTION);
                    }

                    @Override
                    public void onNext(List<VideoSearchItem> videoSearchItems) {
                        adapter.addDataItems(videoSearchItems);
                        mVideoNumber.setText(mTotalVideoNumber + " videos");
                        setLayoutContentType(LayoutContent.LOADED_CONTENT);
                    }
                }));
    }

    private void loadPlaylistVideosNext() {
        compositeSubscriptionForOnStop.add(getPlaylistNextItemsObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoSearchItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        adapter.setErrorLoadingMore(true);
                    }

                    @Override
                    public void onNext(List<VideoSearchItem> videoSearchItems) {
                        if (mNextPageTokern == null || mNextPageTokern.isEmpty()) {
                            adapter.setEndlessScroll(false);
                            if (OnLoadMoreListener != null) {
                                OnLoadMoreListener.setLoadMoreEnable(false);
                            }
                        }
                        adapter.addDataItems(videoSearchItems);
                    }
                }));
    }


    private Observable<List<VideoSearchItem>> getPlaylistItemObservable() {
        return KApplication.rxYoutubeAPIService
                .getYoutubePlaylistItems(mPlaylistId)
                .flatMap(new Func1<ResponsePlaylist, Observable<VideoSearchItem>>() {
                    @Override
                    public Observable<VideoSearchItem> call(ResponsePlaylist responsePlaylist) {
                        mTotalVideoNumber = responsePlaylist.getPageInfo().getTotalResults();
                        mNextPageTokern = responsePlaylist.getNextPageToken();
                        return Observable.from(responsePlaylist.getItems())
                                .subscribeOn(Schedulers.newThread())
                                .flatMap(items -> getStatisticsContentDetails(items.getSnippet()));
                    }
                })
                .toList();
    }

    private Observable<List<VideoSearchItem>> getPlaylistNextItemsObservable() {
        return KApplication.rxYoutubeAPIService
                .getYoutubePlaylistItemsNextPage(mPlaylistId, mNextPageTokern)
                .flatMap(new Func1<ResponsePlaylist, Observable<VideoSearchItem>>() {
                    @Override
                    public Observable<VideoSearchItem> call(ResponsePlaylist responsePlaylist) {
                        mNextPageTokern = responsePlaylist.getNextPageToken();
                        return Observable.from(responsePlaylist.getItems())
                                .subscribeOn(Schedulers.newThread())
                                .flatMap(items -> getStatisticsContentDetails(items.getSnippet()));
                    }
                })
                .toList();
    }

    private Observable<VideoSearchItem> getStatisticsContentDetails(Snippet playlistItem) {
        return KApplication.rxYoutubeAPIService.getStatisticContentDetailById(playlistItem.getResourceId().getVideoId())
                .map(responseStatisticContentDetails
                        -> new VideoSearchItem(playlistItem.getResourceId().getVideoId(), playlistItem.getTitle(),
                        responseStatisticContentDetails.getDurationISO8601Format(),
                        responseStatisticContentDetails.getViewCount(),
                        responseStatisticContentDetails.getLikeCount(),
                        playlistItem.getThumbnails()));
    }


    private void setLayoutContentType(LayoutContent contentType) {
        switch (contentType) {
            case LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mLayoutMainContent.setVisibility(View.GONE);
                mLayoutConnectionError.setVisibility(View.GONE);
                mLayoutContentErrorLoading.setVisibility(View.GONE);
                break;
            case LOADED_CONTENT:
                mProgressBar.setVisibility(View.GONE);
                mLayoutMainContent.setVisibility(View.VISIBLE);
                mLayoutConnectionError.setVisibility(View.GONE);
                mLayoutContentErrorLoading.setVisibility(View.GONE);
                break;
            case ERROR_CONNECTION:
                mProgressBar.setVisibility(View.GONE);
                mLayoutMainContent.setVisibility(View.GONE);
                mLayoutConnectionError.setVisibility(View.VISIBLE);
                mLayoutContentErrorLoading.setVisibility(View.GONE);
                break;
            case ERROR_INTERNAL:
                mProgressBar.setVisibility(View.GONE);
                mLayoutMainContent.setVisibility(View.GONE);
                mLayoutConnectionError.setVisibility(View.GONE);
                mLayoutContentErrorLoading.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopupMenuClick(EventPopupMenuItemClick event) {
        String videoId = event.getData().getVideoId();
        FavoriteRealm inserted = realm.where(FavoriteRealm.class).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, event.getData().getVideoId()).findFirst();

        switch (event.getAction()) {
            case EventPopupMenuItemClick.ACTION.ADD_FAVORITE:
                realm.executeTransaction(realm1 -> {
                    FavoriteRealm favoriteVideo = realm1.createObject(FavoriteRealm.class, System.currentTimeMillis());
                    favoriteVideo.setVideo_id(videoId);
                    Toast.makeText(this, KApplication.appResource.getString(R.string.toast_added_favorite), Toast.LENGTH_SHORT).show();
                });
                // Add video id to favorite change set
                // Or remove if the video id is already in the set
                if (!mFavoriteStateSet.add(videoId)) {
                    mFavoriteStateSet.remove(videoId);
                }
                break;
            case EventPopupMenuItemClick.ACTION.REMOVE_FAVORITE:
                realm.executeTransaction(realm1 -> inserted.deleteFromRealm());
                Toast.makeText(this, KApplication.appResource.getString(R.string.toast_removed_favorite), Toast.LENGTH_SHORT).show();
                // Add video id to favorite change set
                // Or remove if the video id is already in the set
                if (!mFavoriteStateSet.add(videoId)) {
                    mFavoriteStateSet.remove(videoId);
                }
                break;
        }
    }

    enum LayoutContent {
        LOADING,
        LOADED_CONTENT,
        ERROR_CONNECTION,
        ERROR_INTERNAL
    }

    public interface PLAYLIST_TITLE {
        String BOLERO = "Nhạc Trữ Tình";
        String POP = "Nhạc Trẻ";
    }
}
