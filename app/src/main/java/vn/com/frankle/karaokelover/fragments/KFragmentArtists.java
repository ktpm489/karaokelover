package vn.com.frankle.karaokelover.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.activities.KActivityHome;
import vn.com.frankle.karaokelover.adapters.KFragmentAdapterArtists;

/**
 * Created by duclm on 9/21/2016.
 */

public class KFragmentArtists extends Fragment {

    public static final String TAG = KFragmentArtists.class.getSimpleName();

    @BindView(R.id.viewpager_artist)
    ViewPager mViewPager;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.layout_fragment_artist_list, container, false);

        ButterKnife.bind(this, layout);

        mViewPager.setAdapter(new KFragmentAdapterArtists(getFragmentManager(), mContext));
        mViewPager.setOffscreenPageLimit(2);
        ((KActivityHome) mContext).getTabLayout().setupWithViewPager(mViewPager);

        return layout;
    }
}
