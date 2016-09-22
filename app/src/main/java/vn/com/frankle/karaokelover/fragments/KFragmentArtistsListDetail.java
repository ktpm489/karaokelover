package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KActivityArtistDetails;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener;
import vn.com.frankle.karaokelover.adapters.KAdapterZingArtist;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ResponseListArtist;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtist;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 9/19/2016.
 */

public class KFragmentArtistsListDetail extends Fragment {

    public static final String TAG = "FRAGMENT_ARTISTS";
    private static final String DEBUG_TAG = KFragmentArtistsListDetail.class.getSimpleName();
    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    @BindView(R.id.progressbar_artists)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_artists)
    RecyclerView mRecyclerView;
    private Context mContext;
    private int mArtistType;
    private KAdapterZingArtist mAdapter;

    public static KFragmentArtistsListDetail newInstance(int artistType) {
        Log.d(DEBUG_TAG, "Create fragment for artistype = " + artistType);
        Bundle args = new Bundle();
        args.putInt("ARTIST_TYPE", artistType);
        KFragmentArtistsListDetail fragment = new KFragmentArtistsListDetail();
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
        args.putString("artist", clickedArtist.getName());
        artistDetails.putExtras(args);
        mContext.startActivity(artistDetails);
    }

    /**
     * Load artist list from Zing Mp3's server
     */
    private void loadArtistList() {
        setLoadingState(true);

        try {
            String url = ZingMp3API.getListArtistURL(mArtistType, 0);

            Observable<ResponseListArtist> getArtistRequest = KApplication.getRxZingMp3APIService().getArtist(url);
            compositeSubscriptionForOnStop.add(getArtistRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleArtistListResult));
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

            Observable<ResponseListArtist> getArtistRequest = KApplication.getRxZingMp3APIService().getArtist(url);
            compositeSubscriptionForOnStop.add(getArtistRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleArtistLoadMoreResult));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void handleArtistListResult(ResponseListArtist result) {
        if (result.getZingArtists().size() > 0) {
            setLoadingState(false);

            mAdapter.addDataItems(result.getZingArtists());
        } else {
            Toast.makeText(mContext, "No artist.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleArtistLoadMoreResult(ResponseListArtist result) {
        if (result.getZingArtists().size() > 0) {
            mAdapter.addDataItems(result.getZingArtists());
        } else {
            // TO-DO: implement proper handler later
            Toast.makeText(mContext, "No artist.", Toast.LENGTH_SHORT).show();
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
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(mContext, 16), SpaceItemDecoration.VERTICAL));
        mAdapter = new KAdapterZingArtist(mContext);
        Observable<ZingArtist> obsItemClickListener = mAdapter.onItemClickListener();
        compositeSubscriptionForOnStop.add(obsItemClickListener.subscribe(this::handleArtistItemClick));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                loadMoreArtists(totalItemCount);
            }
        });
    }

    /**
     * Switch visiblity of ProgressBar and RecyclerView
     *
     * @param loading : true if display progressbar
     */
    private void setLoadingState(boolean loading) {
        if (loading) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public interface ARTIST_TYPE {
        int VPOP = 1;
        int US_UK = 3;
        int K_POP = 2;
    }
}
