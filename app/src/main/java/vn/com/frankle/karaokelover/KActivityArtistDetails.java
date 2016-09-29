package vn.com.frankle.karaokelover;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterVideoArtistDetail;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
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

    public static final String EXTRA_ARTIST_NAME = "artist_name";
    public static final String EXTRA_ARTIST_ID = "artist_id";
    public static final String EXTRA_AVATAR_URL = "artist_avatar";

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
    @BindView(R.id.progressbar)
    ProgressBar mProgressBar;
    @BindView(R.id.appbar)
    AppBarLayout mAppBar;
    @BindView(R.id.imgv_artist_detail_avatar)
    CircleImageView mArtistAvatar;
    @BindView(R.id.tv_artist_name)
    TextView mArtistNameView;
    @BindView(R.id.layout_music_genres)
    LinearLayout mLayoutMusicGenres;

    private KAdapterVideoArtistDetail mArtistDetailAdapter;


    private String mArtistName;
    private String mArtistId;
    private String mArtistAvatarUrl;
    private String mNextPageToken;


    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get extra data from calling activity
        mArtistName = getIntent().getExtras().getString(EXTRA_ARTIST_NAME);
        mArtistId = getIntent().getExtras().getString(EXTRA_ARTIST_ID);
        mArtistAvatarUrl = getIntent().getExtras().getString(EXTRA_AVATAR_URL);

        // Hacked: hide title set by default behavior
        mCollapsingToolbar.setTitle(" ");

        setupViews();

        loadArtistDetailInfo();

        loadArtistSongs();
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
        mListSongs.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 16), SpaceItemDecoration.VERTICAL));
        mArtistDetailAdapter = new KAdapterVideoArtistDetail(this);
        Observable<VideoSearchItem> itemClickObservable = mArtistDetailAdapter.onItemClickListener();
        compositeSubscriptionForOnStop.add(itemClickObservable.subscribe(this::handleVideoItemClick));
        mListSongs.setAdapter(mArtistDetailAdapter);
        mListSongs.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                loadMoreArtistSongs();
            }
        });
        // Load artist avatar
        Glide.with(this)
                .load(mArtistAvatarUrl)
                .fitCenter()
                .into(mArtistAvatar);
    }

    private void handleVideoItemClick(VideoSearchItem clickedVideoItem) {
        Intent playVideoItent = new Intent(this, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", clickedVideoItem.getTitle());
        playVideoItent.putExtra("videoid", clickedVideoItem.getVideoId());
        startActivity(playVideoItent);
    }

    private void handleDetailInfoResponse(ZingArtistDetail infoData) {
        Glide.with(this)
                .load(ZingMp3API.getZingArtistCoverURL(infoData.getCover3()))
                .fitCenter()
                .into(mArtistCover);

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
                TextView tvGenre = new TextView(this);
                tvGenre.setText(genres.get(i));
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

        // Update biography text
        mArtistDetailAdapter.setArtistInfo(infoData);

        mArtistDetailAdapter.setOnReadMoreListener(() -> {
            KActivityArtistBiography.start(KActivityArtistDetails.this, infoData);
        });
    }

    private void handleYoutubeResponses(List<VideoSearchItem> songs) {
        mArtistDetailAdapter.addDataItems(songs);
        switchLoadingDataState(false);
    }

    private void handleLoadMoreResultResponses(List<VideoSearchItem> songs) {
        mArtistDetailAdapter.addDataItems(songs);
        switchLoadingDataState(false);
    }

    /**
     * Load detailed information of current artist
     */
    private void loadArtistDetailInfo() {

        String jsonArtistData = JSONHelper.writeJsonDataArtistDetail(mArtistId).toString();

        Observable<ZingArtistDetail> obsArtistDetail = KApplication.getRxZingMp3APIService().getArtistDetail(jsonArtistData);
        compositeSubscriptionForOnStop.add(obsArtistDetail
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ZingArtistDetail>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(KActivityArtistDetails.this, "Error while getting artist's detailed information", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ZingArtistDetail zingArtistDetail) {
                        handleDetailInfoResponse(zingArtistDetail);
                    }
                }));
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
