package vn.com.frankle.karaokelover.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterVideoArtistDetail;
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm;
import vn.com.frankle.karaokelover.events.EventFinishLoadingArtistDetailInfoAndVideos;
import vn.com.frankle.karaokelover.events.EventPopupMenuItemClick;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;
import vn.com.frankle.karaokelover.util.JSONHelper;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 8/3/2016.
 */

public class KActivityArtistDetails extends AppCompatActivity {

    private static final String DEBUG_TAG = KActivityArtistDetails.class.getSimpleName();

    public static final String EXTRA_ARTIST_NAME = "artist_name";
    public static final String EXTRA_ARTIST_ID = "artist_id";
    public static final String EXTRA_AVATAR_URL = "artist_avatar";

    enum LayoutType {
        LOADING,
        CONTENT,
        ERROR_CONNECTION
    }

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.list_artist_songs)
    RecyclerView mListSongs;
    @BindView(R.id.imgv_backdrop)
    ImageView mArtistCover;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.appbar)
    AppBarLayout mAppBar;
    @BindView(R.id.imgv_artist_detail_avatar)
    CircleImageView mArtistAvatar;
    @BindView(R.id.tv_artist_name)
    TextView mArtistNameView;
    @BindView(R.id.layout_music_genres)
    LinearLayout mLayoutMusicGenres;
    @BindView(R.id.layout_artist_info_cover)
    RelativeLayout mLayoutArtistInfoCover;
    @BindView(R.id.layout_connection_error)
    RelativeLayout mLayoutConnectionError;
    @BindView(R.id.layout_artist_detail_content)
    RelativeLayout mLayoutContent;
    @BindView(R.id.layout_artist_detail_no_connection)
    RelativeLayout mLayoutConnectionErrorContainer;
    @BindView(R.id.toolbar_no_connection)
    Toolbar mToolbarNoConnection;
    @BindView(R.id.layout_init_loading)
    RelativeLayout mLayoutInitLoading;
    @BindView(R.id.layout_artist_info_genre)
    LinearLayout mLayoutArtistInfoGenre;

    private KAdapterVideoArtistDetail mArtistDetailAdapter;


    private String mArtistName;
    private String mArtistId;
    private String mArtistAvatarUrl;
    private String mNextPageToken;

    private Realm realm;

    private RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem> onItemClickListener = new RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem>() {
        @Override
        public void onDataItemClick(VideoSearchItem dataItem) {
            handleVideoItemClick(dataItem);
        }

        @Override
        public void onErrorLoadMoreRetry() {
            mArtistDetailAdapter.setErrorLoadingMore(false);
            loadMoreArtistSongs();
        }
    };


    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        // Get extra data from calling activity
        mArtistName = getIntent().getExtras().getString(EXTRA_ARTIST_NAME);
        mArtistId = getIntent().getExtras().getString(EXTRA_ARTIST_ID);
        mArtistAvatarUrl = getIntent().getExtras().getString(EXTRA_AVATAR_URL);

        // Hacked: hide title set by default behavior
        mCollapsingToolbar.setTitle(" ");

        mLayoutConnectionError.setOnClickListener(view -> checkInternetConnectionAndInitilaizeViews());

        mToolbarNoConnection.setNavigationOnClickListener(view -> this.finish());

        checkInternetConnectionAndInitilaizeViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        KApplication.eventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        KApplication.eventBus.unregister(this);
    }

    private void checkInternetConnectionAndInitilaizeViews() {
        if (Utils.isOnline(this)) {
            setLayoutType(LayoutType.LOADING);
            setupViews();
            loadArtistDetailInfoAndVideos();
        } else {
            setLayoutType(LayoutType.ERROR_CONNECTION);
        }
    }

    private void setLayoutType(LayoutType layoutType) {
        switch (layoutType) {
            case LOADING:
                mAppBar.setVisibility(View.GONE);
                mLayoutContent.setVisibility(View.GONE);
                mLayoutConnectionErrorContainer.setVisibility(View.VISIBLE);
                mLayoutInitLoading.setVisibility(View.VISIBLE);
                mLayoutConnectionError.setVisibility(View.GONE);
                break;
            case CONTENT:
                mAppBar.setVisibility(View.VISIBLE);
                mLayoutContent.setVisibility(View.VISIBLE);
                mLayoutInitLoading.setVisibility(View.GONE);
                mLayoutConnectionErrorContainer.setVisibility(View.GONE);
                break;
            case ERROR_CONNECTION:
                mAppBar.setVisibility(View.GONE);
                mLayoutContent.setVisibility(View.GONE);
                mLayoutInitLoading.setVisibility(View.GONE);
                mLayoutConnectionError.setVisibility(View.VISIBLE);
                mLayoutConnectionErrorContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupViews() {
        // Setup AppBarLayout height (ratio 16:9)
        float heightDp = getResources().getDisplayMetrics().widthPixels * 9 / 16;
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
        layoutParams.height = (int) heightDp;
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShowing = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mCollapsingToolbar.setTitle(mArtistName);
                    isShowing = true;
                } else if (isShowing) {
                    mCollapsingToolbar.setTitle(" ");
                    isShowing = false;
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListSongs.setLayoutManager(layoutManager);
        mListSongs.setHasFixedSize(true);
        mListSongs.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 0), SpaceItemDecoration.VERTICAL));
        mArtistDetailAdapter = new KAdapterVideoArtistDetail(this, onItemClickListener);
        mListSongs.setAdapter(mArtistDetailAdapter);
        mListSongs.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                loadMoreArtistSongs();
            }
        });
        // Load artist avatar
        if (mArtistAvatarUrl != null) {
            Glide.with(this)
                    .load(mArtistAvatarUrl)
                    .fitCenter()
                    .into(mArtistAvatar);
        } else {
            mArtistAvatar.setVisibility(View.GONE);
            RelativeLayout.LayoutParams genreLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            genreLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLayoutArtistInfoGenre.setLayoutParams(genreLayoutParam);
        }

    }

    private void handleVideoItemClick(VideoSearchItem clickedVideoItem) {
        Intent playVideoItent = new Intent(this, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", clickedVideoItem.getTitle());
        playVideoItent.putExtra("videoid", clickedVideoItem.getVideoId());
        startActivity(playVideoItent);
    }

    private void handleDetailInfoResponse(ZingArtistDetail infoData) {
        String mCoverUrl = ZingMp3API.getZingArtistCoverURL(infoData.getCover3());
        if (mCoverUrl != null) {
            Glide.with(this)
                    .load(mCoverUrl)
                    .fitCenter()
                    .into(mArtistCover);
        }

        mArtistNameView.setText(mArtistName);

        // Add genre view
        List<String> genres = Arrays.asList(infoData.getGenreName().split("\\s*,\\s*"));
        if (genres.size() > 0) {
            float scale = getResources().getDisplayMetrics().density;
            int dpTopBottom = (int) (2 * scale + 0.5f);
            int dpStartEnd = (int) (10 * scale + 0.5f);
            int dpMargin = (int) (4 * scale + 0.5f);
            for (int i = 0; i < genres.size(); i++) {
                if (i > 2) {
                    // We should only allow to display at maximum of 3 genres
                    break;
                }
                String genreName = genres.get(i);
                if (!genreName.trim().isEmpty()) {
                    TextView tvGenre = new TextView(this);
                    tvGenre.setText(genreName);
                    tvGenre.setTextColor(ContextCompat.getColor(this, R.color.text_primary_light));
                    tvGenre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    tvGenre.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_bg_music_genre));
                    tvGenre.setPadding(dpStartEnd, dpTopBottom, dpStartEnd, dpTopBottom);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMarginEnd(dpMargin);
                    tvGenre.setLayoutParams(lp);
                    mLayoutMusicGenres.addView(tvGenre);
                }
            }
        }

        // Update biography text
        mArtistDetailAdapter.setArtistInfo(infoData);

        mArtistDetailAdapter.setOnReadMoreListener(() -> {
            String biography = infoData.getBiography();
            if (biography != null && !biography.trim().isEmpty()) {
                KActivityArtistBiography.start(KActivityArtistDetails.this, infoData);
            } else {
                Toast.makeText(this, getResources().getString(R.string.info_biography_not_available), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleYoutubeResponses(List<VideoSearchItem> songs) {
        mArtistDetailAdapter.addDataItems(songs);
    }

    private void handleLoadMoreResultResponses(List<VideoSearchItem> songs) {
        mArtistDetailAdapter.addDataItems(songs);
    }

    /**
     * Load detailed information of current artist
     */
    private Observable<ZingArtistDetail> getObservableArtistDetailInfo() {

        String jsonArtistData = JSONHelper.writeJsonDataArtistDetail(mArtistId).toString();

        return KApplication.rxZingMp3APIService.getArtistDetail(jsonArtistData);
//        compositeSubscriptionForOnStop.add(obsArtistDetail
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ZingArtistDetail>() {
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setLayoutType(LayoutType.ERROR_CONNECTION);
//                    }
//
//                    @Override
//                    public void onNext(ZingArtistDetail zingArtistDetail) {
//                        handleDetailInfoResponse(zingArtistDetail);
//                        setLayoutType(LayoutType.CONTENT);
//                    }
//                }));
    }

    private Observable<List<VideoSearchItem>> getObservableArtistSongs() {

        String karaokeQuery = mArtistName + " karaoke";

        return KApplication.rxYoutubeAPIService
                .searchKaraokeVideos(karaokeQuery)
                .concatMap(
                        responseSearch -> {
                            mNextPageToken = responseSearch.getNextPageToken();
                            return Observable.from(responseSearch.getItems())
                                    .subscribeOn(Schedulers.newThread())
                                    .concatMap(ReactiveHelper::getStatisticsContentDetails);
                        })
                .toList();
//        compositeSubscriptionForOnStop.add(
//                obsGetArtistSong.subscribeOn(Schedulers.newThread())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Subscriber<List<VideoSearchItem>>() {
//                            @Override
//                            public void onCompleted() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                set(LayoutType.ERROR_CONNECTION);
//                            }
//
//                            @Override
//                            public void onNext(List<VideoSearchItem> videoSearchItems) {
//                                handleYoutubeResponses(videoSearchItems);
//                            }
//                        }));
    }

    private void loadArtistDetailInfoAndVideos() {
        Observable<EventFinishLoadingArtistDetailInfoAndVideos> networkRequest = Observable.zip(getObservableArtistDetailInfo(),
                getObservableArtistSongs(), EventFinishLoadingArtistDetailInfoAndVideos::new)
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        // Preventing memory leak (other Observables: Put, Delete emit result once so memory leak won't live long)
        // Because rx.Observable from Get Operation is endless (it watches for changes of tables from query)
        // You can easily create memory leak (in this case you'll leak the Fragment and all it's fields)
        // So please, PLEASE manage your subscriptions
        // We suggest same mechanism via storing all subscriptions that you want to unsubscribe
        // In something like CompositeSubscription and unsubscribe them in appropriate moment of component lifecycle
        compositeSubscriptionForOnStop.add(networkRequest.subscribe(new Subscriber<EventFinishLoadingArtistDetailInfoAndVideos>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                setLayoutType(LayoutType.ERROR_CONNECTION);
            }

            @Override
            public void onNext(EventFinishLoadingArtistDetailInfoAndVideos data) {
                setLayoutType(LayoutType.CONTENT);

                handleDetailInfoResponse(data.getArtistDetailInfo());
                mArtistDetailAdapter.addDataItems(data.getArtistVideos());
            }
        }));
    }

    private void loadMoreArtistSongs() {
        String karaokeQuery = mArtistName + " karaoke";

        Observable<List<VideoSearchItem>> obsLoadMoreRequest = KApplication.rxYoutubeAPIService
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
                .subscribe(new Subscriber<List<VideoSearchItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mArtistDetailAdapter.setErrorLoadingMore(true);
                    }

                    @Override
                    public void onNext(List<VideoSearchItem> videoSearchItems) {
                        mArtistDetailAdapter.addDataItems(videoSearchItems);
                    }
                }));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopupMenuClick(EventPopupMenuItemClick event) {
        FavoriteRealm inserted = realm.where(FavoriteRealm.class).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, event.getData().getVideoId()).findFirst();

        switch (event.getAction()) {
            case EventPopupMenuItemClick.ACTION.ADD_FAVORITE:
                if (inserted != null) {
                    Toast.makeText(this, "This video is already in the favorite list", Toast.LENGTH_SHORT).show();
                } else {
                    realm.executeTransaction(realm1 -> {
                        FavoriteRealm favoriteVideo = realm1.createObject(FavoriteRealm.class, System.currentTimeMillis());
                        favoriteVideo.setVideo_id(event.getData().getVideoId());
                        Toast.makeText(this, "Added to the favorite list", Toast.LENGTH_SHORT).show();
                    });
                }
                break;
            case EventPopupMenuItemClick.ACTION.REMOVE_FAVORITE:
                realm.executeTransaction(realm1 -> inserted.deleteFromRealm());
                Toast.makeText(this, "Removed from the favorite list", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
        realm.close();
    }
}