package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderBase;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderComment;
import vn.com.frankle.karaokelover.services.responses.youtube.commentthread.CommentThread;

/**
 * Created by duclm on 10/11/2016.
 */

public class KAdapterComments extends RecyclerViewEndlessScrollBaseAdapter<CommentThread> {


    public KAdapterComments(Context context, OnItemClickListener listener) {
        super(context, listener);
    }

    public KAdapterComments(Context context, ArrayList<CommentThread> data) {
        super(context, data);
    }

    @Override
    protected ViewHolderComment createView(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View commentItemView = layoutInflater.inflate(R.layout.recyclerview_item_comment, parent, false);
        return new ViewHolderComment(commentItemView);
    }

    @Override
    protected void bindView(CommentThread item, ViewHolderBase<CommentThread> viewHolder) {
        if (viewHolder instanceof ViewHolderComment) {
            viewHolder.bindData(mContext, item);
        }
    }
}
