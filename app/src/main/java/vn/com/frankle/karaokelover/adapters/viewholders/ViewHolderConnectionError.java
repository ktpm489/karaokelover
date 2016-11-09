package vn.com.frankle.karaokelover.adapters.viewholders;

import android.content.Context;
import android.view.View;

/**
 * Created by duclm on 11/6/2016.
 */

public class ViewHolderConnectionError extends ViewHolderBase<Object> {

    public ViewHolderConnectionError(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(Context context, Object dataItem) {
        // Do nothing
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.CONNECTION_ERROR;
    }
}
