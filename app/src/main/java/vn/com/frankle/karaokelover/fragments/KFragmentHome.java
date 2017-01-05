package vn.com.frankle.karaokelover.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.github.ybq.parallaxviewpager.ParallaxViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.KSharedPreference;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.activities.KActivityPlaylist;
import vn.com.frankle.karaokelover.adapters.KHotArtistAdapter;
import vn.com.frankle.karaokelover.adapters.KPagerAdapterHotKaraokeSong;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.events.EventFinishLoadingHotTrendAndArtist;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;
import vn.com.frankle.karaokelover.util.FileUtils;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.views.widgets.InkPageIndicator;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentHome extends Fragment {

    private static final String DEBUG_TAG = KFragmentHome.class.getSimpleName();
    public static final String TAG = KFragmentHome.class.getSimpleName();

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.cover_container_viewpager)
    ParallaxViewPager mCoverContainer;
    @BindView(R.id.viewpager_indicator)
    InkPageIndicator mViewpagerIndicator;
    @BindView(R.id.recycleview_hot_artists)
    RecyclerView mRecycleViewHotArtists;
    @BindView(R.id.progressbar_hot_artist)
    ProgressBar mProgressBarHotArtist;
    @BindView(R.id.layout_content_home)
    NestedScrollView mLayoutContent;
    @BindView(R.id.fragment_home_content)
    RelativeLayout mFragmentHomeContent;
    @BindView(R.id.content_error_loading)
    RelativeLayout mLayoutErrorLoading;
    @BindView(R.id.playlist_pop)
    CardView mPlaylistPop;
    @BindView(R.id.playlist_bolero)
    CardView mPlaylistBolero;

    private Context mContext;
    private KSharedPreference mSharedPrefs;
    private KHotArtistAdapter mHotArtistAdapter;

    public KFragmentHome() {
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
        // Register eventbus (unregister onDestroy)
//        KApplication.eventBus.register(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreateView");

        View layout = inflater.inflate(R.layout.layout_fragment_home, container, false);

        ButterKnife.bind(this, layout);

        mLayoutErrorLoading.setOnClickListener(v -> checkInternetConnectionAndInitilaizeViews());

        checkInternetConnectionAndInitilaizeViews();

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
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
        // Unregister eventBus
//        KApplication.eventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        Log.d(DEBUG_TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    private void checkInternetConnectionAndInitilaizeViews() {
        if (Utils.isOnline(mContext)) {
            setupViews();
            setContentLayoutType(LayoutType.CONTENT);
            setViewLoadingState(true);
            retrieveHotTrendAndArtistsKaraokes();
        } else {
            setViewLoadingState(false);
            setContentLayoutType(LayoutType.CONNECTION_ERROR);
        }
    }

    private void setupViews() {
        setContentLayoutType(LayoutType.CONTENT);
        // Set up viewpager of hot song content to aspect ration of 16:9
        DisplayMetrics displayMetric = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
        mCoverContainer.getLayoutParams().height = displayMetric.widthPixels * 9 / 16;

        mHotArtistAdapter = new KHotArtistAdapter(mContext);
        mRecycleViewHotArtists.setAdapter(mHotArtistAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewHotArtists.setHasFixedSize(true);
        mRecycleViewHotArtists.setLayoutManager(layoutManager);
        mRecycleViewHotArtists.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 16), SpaceItemDecoration.VERTICAL));

        mPlaylistBolero.setOnClickListener(view -> {
            Intent playlistIntent = new Intent(mContext, KActivityPlaylist.class);
            playlistIntent.putExtra(KActivityPlaylist.EXTRA_PLAYLIST_ID, "PLUbkgCMcrankMiOxyHhN4RSxDgE1JRVIi");
            playlistIntent.putExtra(KActivityPlaylist.EXTRA_TITLE, KActivityPlaylist.PLAYLIST_TITLE.BOLERO);
            startActivity(playlistIntent);
        });

        mPlaylistPop.setOnClickListener(view -> {
            Intent playlistIntent = new Intent(mContext, KActivityPlaylist.class);
            playlistIntent.putExtra(KActivityPlaylist.EXTRA_PLAYLIST_ID, "PL75PeaMqYS56hHy9Obnj2JlQmYy-vgxox");
            playlistIntent.putExtra(KActivityPlaylist.EXTRA_TITLE, KActivityPlaylist.PLAYLIST_TITLE.POP);
            startActivity(playlistIntent);
        });
    }

    /**
     * Get observable for hot karaoke trends
     */
    private Observable<List<ResponseSnippetStatistics>> getObservableHotTrendVideos() {
        ArrayList<String> trendVideoIdList = new ArrayList<>();
        String trendVideosJson = FileUtils.loadDefaultDataJSON(mContext);

        try {
            JSONObject jsonObject = new JSONObject(trendVideosJson);
            JSONArray listVideoIdJson = jsonObject.getJSONArray("trend_id");
            for (int i = 0; i < listVideoIdJson.length(); i++) {
                trendVideoIdList.add(listVideoIdJson.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Observable.from(trendVideoIdList)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(s -> KApplication.rxYoutubeAPIService.getYoutubeVideoById(s))
                .toList();
    }

    /**
     * Get observable for list of host artists and their karaokes that will be displayed on Home screen
     */
    private Observable<List<ArtistWithKaraoke>> getObservableHotArtistsListWithKaraokes() {
        mSharedPrefs = new KSharedPreference(mContext);

        ArrayList<String> favouriteArtistList = mSharedPrefs.getFavouriteArtists(mContext);
        Log.d(DEBUG_TAG, "favouriteArtistList = " + Arrays.toString(favouriteArtistList.toArray()));

        return Observable.from(favouriteArtistList)
                .flatMap(s -> KApplication.rxYoutubeAPIService.searchKaraokeVideos(s + " karaoke", 5)
                        .flatMap(responseSearch -> Observable.from(responseSearch.getItems())
                                .flatMap(ReactiveHelper::getStatisticsContentDetails)
                                .toList()
                                .map(videoSearchItems -> new ArtistWithKaraoke(s, null, null, videoSearchItems))))
                .toList();
    }

    private void retrieveHotTrendAndArtistsKaraokes() {
        Observable<EventFinishLoadingHotTrendAndArtist> networkRequest = Observable.zip(getObservableHotTrendVideos(),
                getObservableHotArtistsListWithKaraokes(), EventFinishLoadingHotTrendAndArtist::new)
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        // Preventing memory leak (other Observables: Put, Delete emit result once so memory leak won't live long)
        // Because rx.Observable from Get Operation is endless (it watches for changes of tables from query)
        // You can easily create memory leak (in this case you'll leak the Fragment and all it's fields)
        // So please, PLEASE manage your subscriptions
        // We suggest same mechanism via storing all subscriptions that you want to unsubscribe
        // In something like CompositeSubscription and unsubscribe them in appropriate moment of component lifecycle
        compositeSubscriptionForOnStop.add(networkRequest.subscribe(new Subscriber<EventFinishLoadingHotTrendAndArtist>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                setContentLayoutType(LayoutType.CONNECTION_ERROR);
            }

            @Override
            public void onNext(EventFinishLoadingHotTrendAndArtist eventFinishLoadingHotTrendAndArtist) {
                KPagerAdapterHotKaraokeSong mHotKaraokePagerAdapter = new KPagerAdapterHotKaraokeSong(getChildFragmentManager(), eventFinishLoadingHotTrendAndArtist.getListHotTrendKaraokes());
                mCoverContainer.setAdapter(mHotKaraokePagerAdapter);
                mCoverContainer.setOffscreenPageLimit(4);
                mViewpagerIndicator.setViewPager(mCoverContainer);

                mHotArtistAdapter.updateAdapterData(eventFinishLoadingHotTrendAndArtist.getListHotArtistWithKaraokes());
                setViewLoadingState(false);
            }
        }));
    }

    /**
     * Switch visiblity of ProgressBar and HotArtist RecyclerView
     *
     * @param loading : true if display progressbar
     */
    private void setViewLoadingState(boolean loading) {
        if (loading) {
            mProgressBarHotArtist.setVisibility(View.VISIBLE);
            mFragmentHomeContent.setVisibility(View.GONE);
        } else {
            mProgressBarHotArtist.setVisibility(View.GONE);
            mFragmentHomeContent.setVisibility(View.VISIBLE);
        }
    }

    private void setContentLayoutType(LayoutType typeLayout) {
        switch (typeLayout) {
            case CONTENT:
                mLayoutContent.setVisibility(View.VISIBLE);
                mLayoutErrorLoading.setVisibility(View.GONE);
                break;
            case CONNECTION_ERROR:
                mLayoutContent.setVisibility(View.GONE);
                mLayoutErrorLoading.setVisibility(View.VISIBLE);
                break;
        }
    }

    enum LayoutType {
        CONTENT,
        CONNECTION_ERROR,
        INTERNAL_ERROR
    }
}
