package vn.com.frankle.karaokelover.adapters.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by duclm on 9/22/2016.
 */

public abstract class ViewHolderBase<T> extends RecyclerView.ViewHolder {

    public ViewHolderBase(View itemView) {
        super(itemView);
    }

    public abstract void bindData(final Context context, T dataItem);

    public abstract int getViewType();

    public interface VIEW_TYPE {
        int HEADER = 0;
        int DATA_ITEM = 1;
        int LOADING_INDICATOR = 2;
        int CONNECTION_ERROR = 3;
    }
}
