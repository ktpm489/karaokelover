package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtist;

/**
 * Created by duclm on 9/22/2016.
 */

public class KAdapterZingArtist extends RecyclerViewEndlessScrollBaseAdapter<ZingArtist> {


    public KAdapterZingArtist(Context context) {
        super(context);
    }

    @Override
    protected ViewHolderBase<ZingArtist> createView(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View artistItemView = layoutInflater.inflate(R.layout.recyclerview_item_artist_zing, parent, false);
        return new ViewHolderArtistItem(artistItemView);
    }

    @Override
    protected void bindView(ZingArtist item, ViewHolderBase<ZingArtist> viewHolder) {
        if (viewHolder instanceof ViewHolderArtistItem) {
            viewHolder.bindData(mContext, item);
        }
    }
}
