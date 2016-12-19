package vn.com.frankle.karaokelover.adapters.viewholders;

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
    @BindView(R.id.btn_bio_more)
    TextView mBtnMore;

    public ViewHolderArtistBio(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Context context, ZingArtistDetail dataItem) {
        String biography = dataItem.getBiography();
        if (biography != null && !biography.trim().isEmpty()) {
            mArtistBio.setText(dataItem.getBiography());
        } else {
            mArtistBio.setText(R.string.info_biography_not_available);
            mBtnMore.setVisibility(View.GONE);
        }

    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.HEADER;
    }
}
