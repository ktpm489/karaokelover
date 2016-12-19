package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderArtistBio;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderBase;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderVideoItem;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;

/**
 * Created by duclm on 9/22/2016.
 */

public class KAdapterVideoArtistDetail extends RecyclerViewEndlessScrollBaseAdapter<VideoSearchItem> {

    private ZingArtistDetail mArtistInfo;
    private OnReadMoreClickListener onReadMoreListener;

    public KAdapterVideoArtistDetail(Context context, OnItemClickListener<VideoSearchItem> onItemClickListener) {
        super(context, onItemClickListener);
        mArtistInfo = null;
    }

    public KAdapterVideoArtistDetail(Context context, ArrayList<VideoSearchItem> data) {
        super(context, data);
        mArtistInfo = null;
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ViewHolderBase.VIEW_TYPE.HEADER) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View artistBioView = inflater.inflate(R.layout.recyclerview_item_artist_bio, parent, false);
            return new ViewHolderArtistBio(artistBioView);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        if (ViewHolderBase.VIEW_TYPE.HEADER == holder.getViewType()) {
            if (mArtistInfo != null) {
                ((ViewHolderArtistBio) holder).bindData(mContext, mArtistInfo);
                if (onReadMoreListener != null) {
                    ((ViewHolderArtistBio) holder).itemView.setOnClickListener(view -> onReadMoreListener.onReadMoreClick());
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() == 0) {
            if (mArtistInfo == null) {
                return 0;
            }
            return 1;
        }
        return mDataList.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ViewHolderBase.VIEW_TYPE.HEADER;
        }
        if (isEndlessScroll) {
            if (position >= mDataList.size() + 1) {
                return isLoadMoreError ? ViewHolderBase.VIEW_TYPE.CONNECTION_ERROR : ViewHolderBase.VIEW_TYPE.LOADING_INDICATOR;
            }
        }
        return ViewHolderBase.VIEW_TYPE.DATA_ITEM;
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

    /**
     * Set artist info and notify recyclerview to update
     */
    public void setArtistInfo(ZingArtistDetail artistInfo) {
        this.mArtistInfo = artistInfo;
        notifyDataSetChanged();
    }

    public void setOnReadMoreListener(OnReadMoreClickListener listener) {
        this.onReadMoreListener = listener;
    }

    public interface OnReadMoreClickListener {
        void onReadMoreClick();
    }
}
