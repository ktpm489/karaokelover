package vn.com.frankle.karaokelover.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.activities.KActivityArtistDetails;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterZingArtist;
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ResponseListArtist;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtist;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 9/19/2016.
 */

public class KFragmentZingArtist extends Fragment {

    public static final String TAG = "FRAGMENT_ARTISTS";
    private static final String DEBUG_TAG = KFragmentZingArtist.class.getSimpleName();
    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.progressbar_artists)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_artists)
    RecyclerView mRecyclerView;
    @BindView(R.id.content_error_loading)
    RelativeLayout mErrorLoading;

    private Context mContext;
    private int mArtistType;
    private KAdapterZingArtist mAdapter;
    private int mCurrentTotalArtistCount;
    private EndlessRecyclerViewScrollListener onScrollListener;

    private RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<ZingArtist> mOnItemClickListener = new RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<ZingArtist>() {
        @Override
        public void onDataItemClick(ZingArtist dataItem) {
            handleArtistItemClick(dataItem);
        }

        @Override
        public void onErrorLoadMoreRetry() {
            mAdapter.setErrorLoadingMore(false);
            loadMoreArtists(mCurrentTotalArtistCount);
        }
    };

    private View.OnClickListener onErrorRetryClickListener = view -> loadArtistList();


    public static KFragmentZingArtist newInstance(int artistType) {
        Bundle args = new Bundle();
        args.putInt("ARTIST_TYPE", artistType);
        KFragmentZingArtist fragment = new KFragmentZingArtist();
        fragment.setArguments(args);
        return fragment;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArtistType = getArguments().getInt("ARTIST_TYPE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.layout_fragment_artists, container, false);

        ButterKnife.bind(this, layout);

        setupFavoriteView();

        loadArtistList();

        return layout;
    }

    private void handleArtistItemClick(ZingArtist clickedArtist) {
        Intent artistDetails = new Intent(mContext, KActivityArtistDetails.class);
        Bundle args = new Bundle();
        args.putString(KActivityArtistDetails.EXTRA_ARTIST_NAME, clickedArtist.getName());
        args.putString(KActivityArtistDetails.EXTRA_ARTIST_ID, String.valueOf(clickedArtist.getArtistId()));
        args.putString(KActivityArtistDetails.EXTRA_AVATAR_URL, ZingMp3API.getZingArtistAvatarURL(clickedArtist.getAvatar()));
        artistDetails.putExtras(args);
        mContext.startActivity(artistDetails);
    }

    /**
     * Load artist list from Zing Mp3's server
     */
    private void loadArtistList() {
        setViewTypeVisibitiy(ViewType.LOADING);
        try {
            String url = ZingMp3API.getListArtistURL(mArtistType, 0);

            Observable<ResponseListArtist> getArtistRequest = KApplication.rxZingMp3APIService.getArtist(url);
            compositeSubscriptionForOnStop.add(getArtistRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ResponseListArtist>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            setViewTypeVisibitiy(ViewType.ERROR);
                        }

                        @Override
                        public void onNext(ResponseListArtist responseListArtist) {
                            handleArtistListResult(responseListArtist);
                        }
                    }));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle load more artists request
     *
     * @param totalItemCount : current total items in the list
     */
    private void loadMoreArtists(int totalItemCount) {
        try {
            String url = ZingMp3API.getListArtistURL(mArtistType, totalItemCount);

            Observable<ResponseListArtist> getArtistRequest = KApplication.rxZingMp3APIService.getArtist(url);
            compositeSubscriptionForOnStop.add(getArtistRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ResponseListArtist>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            mAdapter.setErrorLoadingMore(true);
                        }

                        @Override
                        public void onNext(ResponseListArtist responseListArtist) {
                            handleArtistLoadMoreResult(responseListArtist);
                        }
                    }));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void handleArtistListResult(ResponseListArtist result) {
        if (result.getZingArtists().size() > 0) {
            setViewTypeVisibitiy(ViewType.DISPLAY_DATA);
            mAdapter.addDataItems(result.getZingArtists());
        } else {
            setViewTypeVisibitiy(ViewType.ERROR);
        }
    }

    private void handleArtistLoadMoreResult(ResponseListArtist result) {
        if (result.getZingArtists().size() > 0) {
            mAdapter.addDataItems(result.getZingArtists());
        } else {
            // TO-DO: implement proper handler later
            mAdapter.setEndlessScroll(false);
            onScrollListener.setLoadMoreEnable(false);
        }
    }

    /**
     * Setup view for displaying search result
     */
    private void setupFavoriteView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 0), SpaceItemDecoration.VERTICAL));
        mAdapter = new KAdapterZingArtist(mContext, mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        onScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                mCurrentTotalArtistCount = totalItemCount;
                loadMoreArtists(mCurrentTotalArtistCount);
            }
        };
        mRecyclerView.addOnScrollListener(onScrollListener);

        mErrorLoading.setOnClickListener(onErrorRetryClickListener);
    }

    private void setViewTypeVisibitiy(ViewType displayContent) {
        switch (displayContent) {
            case LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mErrorLoading.setVisibility(View.GONE);
                break;
            case DISPLAY_DATA:
                mProgressBar.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mProgressBar.setVisibility(View.GONE);
                            mErrorLoading.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
                break;
            case ERROR:
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mErrorLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    public interface ARTIST_TYPE {
        int VPOP = 1;
        int US_UK = 3;
        int K_POP = 2;
    }

    private enum ViewType {
        LOADING,
        DISPLAY_DATA,
        ERROR
    }
}
