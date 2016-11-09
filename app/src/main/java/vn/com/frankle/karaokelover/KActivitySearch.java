package vn.com.frankle.karaokelover;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterYoutubeVideoSearch;
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.util.AnimUtils;
import vn.com.frankle.karaokelover.util.ImeUtils;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.util.ViewUtils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.views.widgets.BaselineGridTextView;

/**
 * Created by duclm on 7/31/2016.
 */

public class KActivitySearch extends AppCompatActivity {
    public static final String DEBUG_TAG = KActivitySearch.class.getSimpleName();

    public static final String EXTRA_MENU_LEFT = "EXTRA_MENU_LEFT";
    public static final String EXTRA_MENU_CENTER_X = "EXTRA_MENU_CENTER_X";
    public static final String EXTRA_QUERY = "EXTRA_QUERY";
    public static final int RESULT_CODE_SAVE = 7;

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    @BindView(R.id.searchback)
    ImageButton searchBack;
    @BindView(R.id.searchback_container)
    ViewGroup searchBackContainer;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.search_background)
    View searchBackground;
    @BindView(android.R.id.empty)
    ProgressBar progress;
    @BindView(R.id.search_results)
    RecyclerView results;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.search_toolbar)
    ViewGroup searchToolbar;
    @BindView(R.id.results_container)
    ViewGroup resultsContainer;
    @BindView(R.id.scrim)
    View scrim;
    @BindView(R.id.results_scrim)
    View resultsScrim;
    @BindDimen(R.dimen.z_app_bar)
    float appBarElevation;

    @BindView(R.id.layout_connection_error)
    RelativeLayout mLayoutContentError;
    @BindView(R.id.container_error_connection)
    FrameLayout mContainerError;


    private BaselineGridTextView noResults;
    private Transition auto;

    private int searchBackDistanceX;
    private int searchIconCenterX;
    private boolean dismissing;
    private String mCurrentSearchQuery;
    private String mNextPageToken;

    private KAdapterYoutubeVideoSearch mSearchAdapter;

    private RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem> mOnItemClickListener = new RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem>() {
        @Override
        public void onDataItemClick(VideoSearchItem dataItem) {
            handleOnVideoClickListener(dataItem);
        }

        @Override
        public void onErrorLoadMoreRetry() {
            mSearchAdapter.setErrorLoadingMore(false);
            searchMoreVideo();
        }
    };

    public static Intent createStartIntent(Context context, int menuIconLeft, int menuIconCenterX) {
        Intent starter = new Intent(context, KActivitySearch.class);
        starter.putExtra(EXTRA_MENU_LEFT, menuIconLeft);
        starter.putExtra(EXTRA_MENU_CENTER_X, menuIconCenterX);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_search);
        ButterKnife.bind(this);
        setupSearchView();
        setupSearchResultView();
        auto = TransitionInflater.from(this).inflateTransition(R.transition.auto);


        // extract the search icon's location passed from the launching activity, minus 4dp to
        // compensate for different paddings in the views
        searchBackDistanceX = getIntent().getIntExtra(EXTRA_MENU_LEFT, 0) - (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        searchIconCenterX = getIntent().getIntExtra(EXTRA_MENU_CENTER_X, 0);

        // translate icon to match the launching screen then animate back into position
        searchBackContainer.setTranslationX(searchBackDistanceX);
        searchBackContainer.animate()
                .translationX(0f)
                .setDuration(650L)
                .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this));
        // transform from search icon to back icon
        AnimatedVectorDrawable searchToBack = (AnimatedVectorDrawable) ContextCompat
                .getDrawable(this, R.drawable.avd_search_to_back);
        searchBack.setImageDrawable(searchToBack);
        searchToBack.start();
        // for some reason the animation doesn't always finish (leaving a part arrow!?) so after
        // the animation set a static drawable. Also animation callbacks weren't added until API23
        // so using post delayed :(
        // TODO fix properly!!
        searchBack.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchBack.setImageDrawable(ContextCompat.getDrawable(KActivitySearch.this,
                        R.drawable.ic_arrow_back_padded));
            }
        }, 600L);

        // fade in the other search chrome
        searchBackground.animate()
                .alpha(1f)
                .setDuration(300L)
                .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(this));
        searchView.animate()
                .alpha(1f)
                .setStartDelay(400L)
                .setDuration(400L)
                .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(this))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchView.requestFocus();
                        ImeUtils.showIme(searchView);
                    }
                });

        // animate in a scrim over the content behind
        scrim.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrim.getViewTreeObserver().removeOnPreDrawListener(this);
                AnimatorSet showScrim = new AnimatorSet();
                showScrim.playTogether(
                        ViewAnimationUtils.createCircularReveal(
                                scrim,
                                searchIconCenterX,
                                searchBackground.getBottom(),
                                0,
                                (float) Math.hypot(searchBackDistanceX, scrim.getHeight()
                                        - searchBackground.getBottom())),
                        ObjectAnimator.ofArgb(
                                scrim,
                                ViewUtils.BACKGROUND_COLOR,
                                Color.TRANSPARENT,
                                ContextCompat.getColor(KActivitySearch.this, R.color.scrim)));
                showScrim.setDuration(400L);
                showScrim.setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(KActivitySearch
                        .this));
                showScrim.start();
                return false;
            }
        });
        onNewIntent(getIntent());
    }

    /**
     * Video click handler
     *
     * @param video : video is clicked
     */
    private void handleOnVideoClickListener(VideoSearchItem video) {
        Intent playVideoItent = new Intent(this, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", video.getTitle());
        playVideoItent.putExtra("videoid", video.getVideoId());
        startActivity(playVideoItent);
    }

    /**
     * Setup view for displaying search result
     */
    private void setupSearchResultView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        results.setLayoutManager(layoutManager);
        results.setHasFixedSize(true);
        results.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(this, 0), SpaceItemDecoration.VERTICAL));
        mSearchAdapter = new KAdapterYoutubeVideoSearch(this, mOnItemClickListener);
        results.setAdapter(mSearchAdapter);
        results.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                searchMoreVideo();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            mCurrentSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(mCurrentSearchQuery)) {
                searchView.setQuery(mCurrentSearchQuery, false);
                searchYoutubeVideo();
            }
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    protected void onPause() {
        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    @OnClick(R.id.container_error_connection)
    protected void retryOnConnectionError() {
        searchYoutubeVideo();
    }

    @OnClick({R.id.scrim, R.id.searchback})
    protected void dismiss() {
        if (dismissing) return;
        dismissing = true;

        // translate the icon to match position in the launching activity
        searchBackContainer.animate()
                .translationX(searchBackDistanceX)
                .setDuration(600L)
                .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finishAfterTransition();
                    }
                })
                .start();
        // transform from back icon to search icon
        AnimatedVectorDrawable backToSearch = (AnimatedVectorDrawable) ContextCompat
                .getDrawable(this, R.drawable.avd_back_to_search);
        searchBack.setImageDrawable(backToSearch);
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.setBackground(null);
        backToSearch.start();
        // fade out the other search chrome
        searchView.animate()
                .alpha(0f)
                .setStartDelay(0L)
                .setDuration(120L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // prevent clicks while other anims are finishing
                        searchView.setVisibility(View.INVISIBLE);
                    }
                })
                .start();
        searchBackground.animate()
                .alpha(0f)
                .setStartDelay(300L)
                .setDuration(160L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(null)
                .start();
        if (searchToolbar.getZ() != 0f) {
            searchToolbar.animate()
                    .z(0f)
                    .setDuration(600L)
                    .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                    .start();
        }

        // if we're showing search results, circular hide them
        if (resultsContainer.getHeight() > 0) {
            Animator closeResults = ViewAnimationUtils.createCircularReveal(
                    resultsContainer,
                    searchIconCenterX,
                    0,
                    (float) Math.hypot(searchIconCenterX, resultsContainer.getHeight()),
                    0f);
            closeResults.setDuration(500L);
            closeResults.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(KActivitySearch
                    .this));
            closeResults.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resultsContainer.setVisibility(View.INVISIBLE);
                }
            });
            closeResults.start();
        }

        // fade out the scrim
        scrim.animate()
                .alpha(0f)
                .setDuration(400L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(null)
                .start();
    }


    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // hint, inputType & ime options seem to be ignored from XML! Set in code
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentSearchQuery = query;
                searchYoutubeVideo();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.d(DEBUG_TAG, "onQueryTextChange");
                if (TextUtils.isEmpty(query)) {
                    clearResults();
                }
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
        });
    }

    private void clearResults() {
        TransitionManager.beginDelayedTransition(container, auto);
        mSearchAdapter.removeAllDataItem();
        results.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        resultsScrim.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }

    private void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (BaselineGridTextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                noResults.setOnClickListener(v -> {
                    searchView.setQuery("", false);
                    searchView.requestFocus();
                    ImeUtils.showIme(searchView);
                });
            }
            String message = String.format(getString(R
                    .string.no_search_results), searchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noResults.setText(ssb);
        }
        if (noResults != null) {
            noResults.setVisibility(visibility);
        }
    }

    private void handleSearchResult(List<VideoSearchItem> searchResults) {
        Log.i("SearchResult", "Search result size = " + searchResults.size());

        if (searchResults.size() > 0) {
            if (results.getVisibility() != View.VISIBLE) {
                TransitionManager.beginDelayedTransition(container, auto);
                progress.setVisibility(View.GONE);
                results.setVisibility(View.VISIBLE);

                mSearchAdapter.addDataItems(searchResults);
            }
        } else {
            // No result to display
            TransitionManager.beginDelayedTransition(container, auto);
            progress.setVisibility(View.GONE);
            setNoResultsVisibility(View.VISIBLE);
        }
    }

    /**
     * Handle result of loading more videos
     *
     * @param loadMoreResult : the loaded more result
     */
    private void handleResultLoadMore(List<VideoSearchItem> loadMoreResult) {
        if (loadMoreResult.size() > 0) {
            mSearchAdapter.addDataItems(loadMoreResult);
        } else {
            Toast.makeText(this, "There is no more video.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchYoutubeVideo() {
        clearResults();
        ImeUtils.hideIme(searchView);
        searchView.clearFocus();

        if (Utils.isOnline(this)) {
            switchConnectionErrorLayout(false);

            String karaokeQuery = mCurrentSearchQuery + " karaoke";

            Observable<List<VideoSearchItem>> searchRequest = KApplication.getRxYoutubeAPIService()
                    .searchKaraokeVideos(karaokeQuery)
                    .flatMap(
                            responseSearch -> {
                                mNextPageToken = responseSearch.getNextPageToken();
                                return Observable.from(responseSearch.getItems())
                                        .subscribeOn(Schedulers.newThread())
                                        .flatMap(ReactiveHelper::getStatisticsContentDetails);
                            })
                    .toList();
            compositeSubscriptionForOnStop.add(searchRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<VideoSearchItem>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            switchConnectionErrorLayout(true);
                        }

                        @Override
                        public void onNext(List<VideoSearchItem> videoSearchItems) {
                            handleSearchResult(videoSearchItems);
                        }
                    }));
        } else {
            switchConnectionErrorLayout(true);
        }
    }

    /**
     * Load more search result
     */
    private void searchMoreVideo() {
        Log.d(DEBUG_TAG, "Load more videos: mCurrentToken = " + mNextPageToken);
        String karaokeQuery = mCurrentSearchQuery + " karaoke";

        Observable<List<VideoSearchItem>> loadMoreRequest = KApplication.getRxYoutubeAPIService()
                .searchYoutubeVideoNext(karaokeQuery, mNextPageToken)
                .flatMap(
                        responseSearch -> {
                            mNextPageToken = responseSearch.getNextPageToken();
                            return Observable.from(responseSearch.getItems())
                                    .subscribeOn(Schedulers.newThread())
                                    .flatMap(ReactiveHelper::getStatisticsContentDetails);
                        })
                .toList();
        compositeSubscriptionForOnStop.add(loadMoreRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoSearchItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mSearchAdapter.setErrorLoadingMore(true);
                    }

                    @Override
                    public void onNext(List<VideoSearchItem> videoSearchItems) {
                        handleResultLoadMore(videoSearchItems);
                    }
                }));
    }

    private void switchConnectionErrorLayout(boolean isError) {
        if (isError) {
            TransitionManager.beginDelayedTransition(container, auto);
            progress.setVisibility(View.GONE);
            mContainerError.setVisibility(View.VISIBLE);
            mLayoutContentError.setVisibility(View.VISIBLE);
        } else {
            mContainerError.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }
    }
}
