package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.viewpagerindicator.CirclePageIndicator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.yangbingqiang.android.parallaxviewpager.ParallaxViewPager;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.KHotArtistAdapter;
import vn.com.frankle.karaokelover.adapters.KPagerAdapterHotKaraokeSong;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrend;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.HotTrendTable;
import vn.com.frankle.karaokelover.events.EventFinishLoadingHotTrendAndArtist;
import vn.com.frankle.karaokelover.services.ReactiveHelper;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentHome extends Fragment {

    public static final String KEY_PHYSIC_SCREEN_SIZE = "key_physic_screen_size";

    private Context mContext;

    @BindView(R.id.cover_container_viewpager)
    ParallaxViewPager mCoverContainer;
    @BindView(R.id.cover_viewpager_indicator)
    CirclePageIndicator mViewpagerIndicator;
    @BindView(R.id.recycleview_hot_artists)
    RecyclerView mRecycleViewHotArtists;
    @BindView(R.id.progressbar_hot_artist)
    ProgressBar mProgressBarHotArtist;

    // In this sample app we use dependency injection (DI) to keep the code clean
    // Just remember that it's already configured instance of StorIOSQLite from DbModule
    @Inject
    StorIOSQLite storIOSQLite;

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

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
        KApplication.eventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister eventBus
        KApplication.eventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscriptionForOnStop.unsubscribe();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KApplication.get(mContext).appComponent().inject(this);

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

        setupViews();

        retrieveHotTrendAndArtistsKaraokes();

        return layout;
    }

    private void setupViews() {
        // Set up viewpager of hot song content to aspect ration of 16:9
        mCoverContainer.getLayoutParams().height = mPhyScreenWidthInPixel * 9 / 16;

        mHotArtistAdapter = new KHotArtistAdapter(mContext);
        mRecycleViewHotArtists.setAdapter(mHotArtistAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewHotArtists.setHasFixedSize(true);
        mRecycleViewHotArtists.setLayoutManager(layoutManager);
        mRecycleViewHotArtists.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 16), SpaceItemDecoration.VERTICAL));
        setHotArtistLoadingState(true);
    }

    /**
     * Get observable for hot karaoke trends
     *
     * @return
     */
    private Observable<List<ResponseSnippetStatistics>> getObservableHotTrend() {
        return storIOSQLite
                .get()
                .listOfObjects(DAOHotTrend.class)
                .withQuery(HotTrendTable.QUERY_ALL)
                .prepare()
                .asRxObservable()
                .subscribeOn(Schedulers.newThread())
                .concatMap(ReactiveHelper::getObsListHotTrend);
    }

    /**
     * Get observable for list of host artists and their karaokes that will be displayed on Home screen
     */
    private Observable<List<ArtistWithKaraoke>> getObservableHotArtistsListWithKaraokes() {
        return storIOSQLite
                .get()
                .listOfObjects(DAOArtist.class)
                .withQuery(ArtistTable.QUERY_ALL)
                .prepare()
                .asRxObservable()
                .subscribeOn(Schedulers.newThread())
                .concatMap(daoArtists -> ReactiveHelper.getListHotArtistWithKarokes(storIOSQLite, daoArtists))
                .concatMap(ReactiveHelper::getListHotKaraokeOfArtist);
    }

    private void retrieveHotTrendAndArtistsKaraokes() {
        Observable<EventFinishLoadingHotTrendAndArtist> networkRequest = Observable.zip(getObservableHotTrend(),
                getObservableHotArtistsListWithKaraokes(), EventFinishLoadingHotTrendAndArtist::new);
        // Preventing memory leak (other Observables: Put, Delete emit result once so memory leak won't live long)
        // Because rx.Observable from Get Operation is endless (it watches for changes of tables from query)
        // You can easily create memory leak (in this case you'll leak the Fragment and all it's fields)
        // So please, PLEASE manage your subscriptions
        // We suggest same mechanism via storing all subscriptions that you want to unsubscribe
        // In something like CompositeSubscription and unsubscribe them in appropriate moment of component lifecycle
        compositeSubscriptionForOnStop.add(networkRequest.subscribe(eventFinishLoadingHotTrendAndArtist ->
                KApplication.eventBus.post(eventFinishLoadingHotTrendAndArtist)));
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

    /**
     * ---------------------------------------EVENTs HANDLING-------------------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventFinishLoadingHotTrendAndArtistsKaraokes(EventFinishLoadingHotTrendAndArtist event) {
        KPagerAdapterHotKaraokeSong mHotKaraokePagerAdapter = new KPagerAdapterHotKaraokeSong(getFragmentManager(), event.getListHotTrendKaraokes());
        mCoverContainer.setAdapter(mHotKaraokePagerAdapter);
        mCoverContainer.setOffscreenPageLimit(4);
        mViewpagerIndicator.setViewPager(mCoverContainer);

        mHotArtistAdapter.updateAdapterData(event.getListHotArtistWithKaraokes());
        setHotArtistLoadingState(false);
    }
    /**-----------------------------------END OF EVENTs HANDLING----------------------------------*/

}
