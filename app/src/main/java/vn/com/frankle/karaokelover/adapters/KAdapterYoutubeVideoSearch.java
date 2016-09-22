package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 9/22/2016.
 */

public class KAdapterYoutubeVideoSearch extends RecyclerViewEndlessScrollBaseAdapter<VideoSearchItem> {

    public KAdapterYoutubeVideoSearch(Context context) {
        super(context);
    }

    public KAdapterYoutubeVideoSearch(Context context, ArrayList<VideoSearchItem> data) {
        super(context, data);
    }

    @Override
    public ViewHolderVideoItem createView(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View videoItemView = layoutInflater.inflate(R.layout.recyclerview_item_video_search, parent, false);
        return new ViewHolderVideoItem(videoItemView);
    }

    @Override
    protected void bindView(VideoSearchItem item, ViewHolderBase<VideoSearchItem> viewHolder) {
        if (viewHolder instanceof ViewHolderVideoItem) {
            viewHolder.bindData(mContext, item);
        }
    }
}
