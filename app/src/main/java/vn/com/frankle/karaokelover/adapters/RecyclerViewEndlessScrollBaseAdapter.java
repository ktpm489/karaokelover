package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderBase;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderConnectionError;
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderLoadingMore;


/**
 * Created by duclm on 9/22/2016.
 */

public abstract class RecyclerViewEndlessScrollBaseAdapter<T extends Comparable<? super T>> extends RecyclerView.Adapter<ViewHolderBase> {

    protected Context mContext;
    protected ArrayList<T> mDataList;
    private OnItemClickListener mItemListener;

    protected boolean isEndlessScroll;
    protected boolean isLoadMoreError;

    RecyclerViewEndlessScrollBaseAdapter(Context context) {
        this.mContext = context;
        this.mDataList = new ArrayList<>();
        this.isEndlessScroll = true;
    }

    RecyclerViewEndlessScrollBaseAdapter(Context context, OnItemClickListener<T> listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>();
        this.isEndlessScroll = true;
        this.mItemListener = listener;
    }

    RecyclerViewEndlessScrollBaseAdapter(Context context, final ArrayList<T> data) {
        this.mContext = context;
        this.mDataList = data;
        this.isEndlessScroll = true;
    }

    protected abstract ViewHolderBase<T> createView(ViewGroup parent);

    protected abstract void bindView(T item, ViewHolderBase<T> viewHolder);

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case ViewHolderBase.VIEW_TYPE.DATA_ITEM:
                return createView(parent);
            case ViewHolderBase.VIEW_TYPE.LOADING_INDICATOR:
                View loadingMoreView = inflater.inflate(R.layout.recyclerview_item_loading_more, parent, false);
                return new ViewHolderLoadingMore(loadingMoreView);
            case ViewHolderBase.VIEW_TYPE.CONNECTION_ERROR:
                View errorLoadingMoreView = inflater.inflate(R.layout.recyclerview_load_more_error, parent, false);
                return new ViewHolderConnectionError(errorLoadingMoreView);
        }
        return null;
    }

    // TO-DO : improve OOP structure here (remove concrete class compare)
    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        if (ViewHolderBase.VIEW_TYPE.LOADING_INDICATOR == holder.getViewType()) {
            ((ViewHolderLoadingMore) holder).setIndeterminate(true);
        } else if (ViewHolderBase.VIEW_TYPE.DATA_ITEM == holder.getViewType()) {
            bindView(mDataList.get(position), holder);
            if (mItemListener != null) {
                holder.itemView.setOnClickListener(v -> mItemListener.onDataItemClick(mDataList.get(position)));
            }
        } else if (ViewHolderBase.VIEW_TYPE.CONNECTION_ERROR == holder.getViewType()) {
            if (mItemListener != null) {
                holder.itemView.setOnClickListener(v -> mItemListener.onErrorLoadMoreRetry());
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() == 0) {
            return 0;
        }
        if (isEndlessScroll) {
            return mDataList.size() + 1;
        }
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isEndlessScroll) {
            if (position >= mDataList.size()) {
                return isLoadMoreError ? ViewHolderBase.VIEW_TYPE.CONNECTION_ERROR : ViewHolderBase.VIEW_TYPE.LOADING_INDICATOR;
            }
            return ViewHolderBase.VIEW_TYPE.DATA_ITEM;
        }
        return ViewHolderBase.VIEW_TYPE.DATA_ITEM;
    }

    /**
     * Set flag to indicate recyclerview is endless scroll or not
     *
     * @param isEndlessScroll : flag
     */
    public void setEndlessScroll(boolean isEndlessScroll) {
        this.isEndlessScroll = isEndlessScroll;
    }

    /**
     * Set adapter with new list of data
     *
     * @param dataList : new data list
     */
    public void setDataItems(List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    /**
     * Add a single data item to the recyclerview
     *
     * @param dataItem : data item to be added
     */
    public void addDataItem(T dataItem) {
        mDataList.add(dataItem);
        notifyDataSetChanged();
    }

    /**
     * Add a list of data items to the RecyclerView
     *
     * @param dataList : data item list to be added
     */
    public void addDataItems(List<T> dataList) {
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    /**
     * Remove a data item from the RecyclerView
     *
     * @param dataItem : data item to be removed
     */
    public void removeDataItem(T dataItem) {
        for (T item : mDataList) {
            if (item.compareTo(dataItem) == 0) {
                mDataList.remove(item);
                notifyDataSetChanged();
                return;
            }
        }
    }

    /**
     * Remove all item from current data set
     */
    public void removeAllDataItem() {
        if (this.mDataList != null) {
            this.mDataList.clear();
            notifyDataSetChanged();
        }
    }

    public void setErrorLoadingMore(boolean isError) {
        this.isLoadMoreError = isError;
        notifyItemChanged(mDataList.size());
    }

    public interface OnItemClickListener<T> {
        void onDataItemClick(T dataItem);

        void onErrorLoadMoreRetry();
    }
}
