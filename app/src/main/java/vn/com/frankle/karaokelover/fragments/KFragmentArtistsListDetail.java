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
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.KAdapterArtists;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ResponseListArtist;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 9/19/2016.
 */

public class KFragmentArtistsListDetail extends Fragment {

    private static final String DEBUG_TAG = KFragmentArtistsListDetail.class.getSimpleName();
    public static final String TAG = "FRAGMENT_ARTISTS";

    public interface ARTIST_TYPE {
        int VPOP = 1;
        int US_UK = 3;
        int K_POP = 2;
    }

    private Context mContext;

    @BindView(R.id.progressbar_artists)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_artists)
    RecyclerView mRecyclerView;

    private int mArtistType;

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    private KAdapterArtists mAdapter;

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

        loadArtistList(mArtistType, 0);

        return layout;
    }

    /**
     * Load artist list from Zing Mp3's server
     *
     * @param type : type of artist (V-POP, US-UK, K-POP)
     * @param page : page number of results (search result has pagination)
     */
    private void loadArtistList(int type, int page) {
        setLoadingState(true);

        try {
            String url = ZingMp3API.getListArtistURL(type, page);

            Observable<ResponseListArtist> getArtistRequest = KApplication.getRxZingMp3APIService().getArtist(url);
            compositeSubscriptionForOnStop.add(getArtistRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleArtistListReuslt));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void handleArtistListReuslt(ResponseListArtist result) {
        if (result.getDocs().size() > 0) {
            setLoadingState(false);

            mAdapter.populateWithData(result.getDocs());
        } else {
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
        mAdapter = new KAdapterArtists(mContext);
        mRecyclerView.setAdapter(mAdapter);
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
}
