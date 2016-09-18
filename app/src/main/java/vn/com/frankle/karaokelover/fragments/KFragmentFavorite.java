package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;

/**
 * Created by duclm on 9/18/2016.
 */

public class KFragmentFavorite extends Fragment {

    private Context mContext;

    @BindView(R.id.progressbar_favorite)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerview_my_favorite)
    RecyclerView mRecyclerView;

    public KFragmentFavorite() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.layout_fragment_my_favorite, container, false);

        ButterKnife.bind(mContext, layout);

        return layout;
    }
}
