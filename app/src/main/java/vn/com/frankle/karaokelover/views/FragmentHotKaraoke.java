package vn.com.frankle.karaokelover.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.activities.KActivityHome;
import vn.com.frankle.karaokelover.activities.KActivityPlayVideo;
import vn.com.frankle.karaokelover.fragments.KFragmentFavorite;
import vn.com.frankle.karaokelover.util.Utils;

/**
 * Created by duclm on 7/16/2016.
 */

public class FragmentHotKaraoke extends Fragment {

    private static final String DEBUG_TAG = FragmentHotKaraoke.class.getSimpleName();

    private Context mContext;
    private Bundle mBundle;

    @BindView(R.id.layout_viewpager_hot_karaoke)
    RelativeLayout mContentView;
    @BindView(R.id.tv_title_hot_karaoke)
    TextView mTitle;
    @BindView(R.id.tv_sub_hot_count)
    TextView mHotPlayCount;
    @BindView(R.id.tv_sub_hot_like)
    TextView mHotLikeCount;
    @BindView(R.id.imgv_video_thumbnail)
    ImageView mThumbnail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = container.getContext();

        View view = inflater.inflate(R.layout.layout_fragment_hot_karaoke, container, false);
        ButterKnife.bind(this, view);

        view.setOnClickListener(view1 -> handleHotKaraokeClickEvent());

        mTitle.setText(mBundle.getString("title"));
        mHotPlayCount.setText(Utils.getViewCount(mBundle.getString("play_count")));
        mHotLikeCount.setText(Utils.getLikeCount(mBundle.getString("like_count")));
        Glide.with(this).load(mBundle.getString("thumbnail")).into(mThumbnail);

        return view;
    }

    /**
     * Hot karaoke click event handler
     */
    private void handleHotKaraokeClickEvent() {
        Intent playVideoItent = new Intent(mContext, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", mBundle.getString("title"));
        playVideoItent.putExtra("videoid", mBundle.getString("id"));
        ((KActivityHome) mContext).startActivityForResult(playVideoItent, KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST);
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
}
