package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import vn.com.frankle.karaokelover.R;


/**
 * Created by duclm on 9/22/2016.
 */

public abstract class RecyclerViewEndlessScrollBaseAdapter<T extends Comparable<? super T>> extends RecyclerView.Adapter<ViewHolderBase> {

    private final int VIEWTYPE_DATA_ITEM = 0;
    private final int VIEWTYPE_DATA_LOADING = 1;
    private final PublishSubject<T> rxOnClickSubject = PublishSubject.create();
    protected Context mContext;
    private ArrayList<T> mDataList;

    RecyclerViewEndlessScrollBaseAdapter(Context context) {
        this.mContext = context;
        this.mDataList = new ArrayList<>();
    }

    RecyclerViewEndlessScrollBaseAdapter(Context context, final ArrayList<T> data) {
        this.mContext = context;
        this.mDataList = data;
    }

    protected abstract ViewHolderBase<T> createView(ViewGroup parent);

    protected abstract void bindView(T item, ViewHolderBase<T> viewHolder);

    /**
     * Get on item click listener observable
     *
     * @return OnItemClick event observable
     */
    public Observable<T> onItemClickListener() {
        return rxOnClickSubject.asObservable();
    }

    @Override
    public final ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_DATA_ITEM:
                return createView(parent);
            case VIEWTYPE_DATA_LOADING:
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View loadingMoreView = inflater.inflate(R.layout.recyclerview_item_loading_more, parent, false);
                return new ViewHolderLoadingMore(loadingMoreView);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(ViewHolderBase holder, int position) {
        if (holder == null) {
            return;
        }
        if (holder instanceof ViewHolderLoadingMore) {
            ((ViewHolderLoadingMore) holder).setIndeterminate(true);
        } else {
            bindView(mDataList.get(position), holder);
            holder.itemView.setOnClickListener(view -> rxOnClickSubject.onNext(mDataList.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() == 0) {
            return 0;
        }
        return mDataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position >= mDataList.size() ? VIEWTYPE_DATA_LOADING : VIEWTYPE_DATA_ITEM;
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
}
