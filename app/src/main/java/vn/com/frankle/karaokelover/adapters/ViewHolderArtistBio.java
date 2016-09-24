package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;

/**
 * Created by duclm on 9/24/2016.
 */

public class ViewHolderArtistBio extends ViewHolderBase<ZingArtistDetail> {

    @BindView(R.id.tv_artist_bio)
    TextView mArtistBio;

    ViewHolderArtistBio(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Context context, ZingArtistDetail dataItem) {
        mArtistBio.setText(dataItem.getBiography());
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.HEADER;
    }
}
