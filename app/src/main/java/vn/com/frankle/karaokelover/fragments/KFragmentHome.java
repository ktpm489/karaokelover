package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.github.ybq.parallaxviewpager.ParallaxViewPager;
import com.viewpagerindicator.CirclePageIndicator;

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
import vn.com.frankle.karaokelover.adapters.KHotArtistAdapter;
import vn.com.frankle.karaokelover.adapters.KPagerAdapterHotKaraokeSong;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.events.EventFinishLoadingHotTrendAndArtist;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;
import vn.com.frankle.karaokelover.util.FileUtils;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentHome extends Fragment {

    public static final String KEY_PHYSIC_SCREEN_SIZE = "key_physic_screen_size";
    private static final String DEBUG_TAG = KFragmentHome.class.getSimpleName();
    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.cover_container_viewpager)
    ParallaxViewPager mCoverContainer;
    @BindView(R.id.cover_viewpager_indicator)
    CirclePageIndicator mViewpagerIndicator;
    @BindView(R.id.recycleview_hot_artists)
    RecyclerView mRecycleViewHotArtists;
    @BindView(R.id.progressbar_hot_artist)
    ProgressBar mProgressBarHotArtist;
    @BindView(R.id.layout_home_content)
    RelativeLayout mLayoutContent;
    @BindView(R.id.layout_connection_error)
    RelativeLayout mLayoutNoConnection;

    private Context mContext;
    private KSharedPreference mSharedPrefs;
    private int mPhyScreenWidthInPixel;
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
    public void onStop() {
        super.onStop();
        // Unregister eventBus
//        KApplication.eventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        KApplication.get(mContext).appComponent().inject(this);

        mPhyScreenWidthInPixel = getArguments().getInt(KEY_PHYSIC_SCREEN_SIZE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.layout_fragment_home, container, false);

        ButterKnife.bind(this, layout);

        mLayoutNoConnection.setOnClickListener(v -> checkInternetConnectionAndInitilaizeViews());

        checkInternetConnectionAndInitilaizeViews();

        return layout;
    }

    private void checkInternetConnectionAndInitilaizeViews() {
        if (Utils.isOnline(mContext)) {
            setupViews();
            setHotArtistLoadingState(true);
            retrieveHotTrendAndArtistsKaraokes();
        } else {
            setConnectionErrorStateView(true);
        }
    }

    private void setupViews() {
        setConnectionErrorStateView(false);
        // Set up viewpager of hot song content to aspect ration of 16:9
        mCoverContainer.getLayoutParams().height = mPhyScreenWidthInPixel * 9 / 16;

        mHotArtistAdapter = new KHotArtistAdapter(mContext);
        mRecycleViewHotArtists.setAdapter(mHotArtistAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewHotArtists.setHasFixedSize(true);
        mRecycleViewHotArtists.setLayoutManager(layoutManager);
        mRecycleViewHotArtists.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 16), SpaceItemDecoration.VERTICAL));
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
                .flatMap(s -> KApplication.getRxYoutubeAPIService().getYoutubeVideoById(s))
                .toList();
    }

    /**
     * Get observable for list of host artists and their karaokes that will be displayed on Home screen
     */
    private Observable<List<ArtistWithKaraoke>> getObservableHotArtistsListWithKaraokes() {
        mSharedPrefs = new KSharedPreference();

        ArrayList<String> favouriteArtistList = mSharedPrefs.getFavouriteArtists(mContext);
        Log.d(DEBUG_TAG, "favouriteArtistList = " + Arrays.toString(favouriteArtistList.toArray()));

        return Observable.from(favouriteArtistList)
                .flatMap(s -> KApplication.getRxYoutubeAPIService().searchKaraokeVideos(s + " karaoke", 5)
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
                setConnectionErrorStateView(true);
            }

            @Override
            public void onNext(EventFinishLoadingHotTrendAndArtist eventFinishLoadingHotTrendAndArtist) {
                KPagerAdapterHotKaraokeSong mHotKaraokePagerAdapter = new KPagerAdapterHotKaraokeSong(getFragmentManager(), eventFinishLoadingHotTrendAndArtist.getListHotTrendKaraokes());
                mCoverContainer.setAdapter(mHotKaraokePagerAdapter);
                mCoverContainer.setOffscreenPageLimit(4);
                mViewpagerIndicator.setViewPager(mCoverContainer);

                mHotArtistAdapter.updateAdapterData(eventFinishLoadingHotTrendAndArtist.getListHotArtistWithKaraokes());
                setHotArtistLoadingState(false);
            }
        }));
    }

    /**
     * Switch visiblity of ProgressBar and HotArtist RecyclerView
     *
     * @param loading : true if display progressbar
     */
    private void setHotArtistLoadingState(boolean loading) {
        if (loading) {
            mProgressBarHotArtist.setVisibility(View.VISIBLE);
        } else {
            mProgressBarHotArtist.setVisibility(View.GONE);
        }
    }

    private void setConnectionErrorStateView(boolean error) {
        if (error) {
            mLayoutContent.setVisibility(View.GONE);
            mLayoutNoConnection.setVisibility(View.VISIBLE);
        } else {
            mLayoutNoConnection.setVisibility(View.GONE);
            mLayoutContent.setVisibility(View.VISIBLE);
        }
    }
}
