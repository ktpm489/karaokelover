package vn.com.frankle.karaokelover.adapters.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;

/**
 * Created by duclm on 9/22/2016.
 */

public class ViewHolderLoadingMore extends ViewHolderBase<Object> {

    @BindView(R.id.progressbar_load_more)
    ProgressBar mProgressBar;

    public ViewHolderLoadingMore(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Context context, Object dataItem) {
        // Do nothing
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.LOADING_INDICATOR;
    }


    public void setIndeterminate(boolean indeterminate) {
        mProgressBar.setIndeterminate(true);
    }

}
