package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtist;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 9/22/2016.
 */

public class ViewHolderArtistItem extends ViewHolderBase<ZingArtist> {

    @BindView(R.id.list_artist_avatar)
    ImageView mAvatar;
    @BindView(R.id.list_artist_name)
    TextView mArtistName;

    ViewHolderArtistItem(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Context context, ZingArtist dataItem) {
        mArtistName.setText(dataItem.getName());

        Glide.with(context).load(ZingMp3API.getArtistAvatarURL(dataItem.getAvatar())).into(mAvatar);
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.DATA_ITEM;
    }
}
