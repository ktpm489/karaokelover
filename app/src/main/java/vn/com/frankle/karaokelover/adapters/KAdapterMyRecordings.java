package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.util.Utils;

/**
 * Created by duclm on 9/7/2016.
 */

public class KAdapterMyRecordings extends RecyclerView.Adapter<KAdapterMyRecordings.ViewHolderRecording> {

    public static final int EXPAND = 0x1;
    public static final int COLLAPSE = 0x2;

    private int expandedPosition = RecyclerView.NO_POSITION;

    private Context mContext;
    private ArrayList<File> mRecordDataSet;
    private RecordedSongAdapterListener mListener;

    public KAdapterMyRecordings(Context context, File[] listRecordings, RecordedSongAdapterListener onRecordedItemClickListener) {
        this.mContext = context;
        this.mListener = onRecordedItemClickListener;
        this.mRecordDataSet = new ArrayList<>();
        Collections.addAll(mRecordDataSet, listRecordings);
    }

    @Override
    public ViewHolderRecording onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View recordingItem = inflater.inflate(R.layout.recyclerview_item_recorded_song, parent, false);
        return new ViewHolderRecording(mContext, recordingItem);
    }


    @Override
    public void onBindViewHolder(ViewHolderRecording holder, int position) {

        File item = mRecordDataSet.get(position);

        holder.bind(item, position, mListener);
        final boolean isExpanded = position == expandedPosition;
        setExpanded(holder, isExpanded);
    }

    @Override
    public void onBindViewHolder(ViewHolderRecording holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        bindPartialRecordedItemChange(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        if (mRecordDataSet == null) {
            return 0;
        }
        return mRecordDataSet.size();
    }

    /**
     * Remove an item from data set at specific position
     *
     * @param postion : the item at this position will be removed
     */
    public void removeItemAtPosition(int postion) {
        mRecordDataSet.remove(postion);
        notifyDataSetChanged();
    }

    private void setExpanded(ViewHolderRecording holder, boolean isExpanded) {
        holder.itemView.setActivated(isExpanded);
        holder.btnDelete.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.btnShare.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.btnPlay.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    public int getExpandedItemPosition() {
        return expandedPosition;
    }

    public void setExpandedItemPosition(int expandedPosition) {
        this.expandedPosition = expandedPosition;
    }

    private void bindPartialRecordedItemChange(
            ViewHolderRecording holder, int position, List<Object> partialChangePayloads) {
        // for certain changes we don't need to rebind data, just update some view state
        if (partialChangePayloads != null && (partialChangePayloads.contains(EXPAND)
                || partialChangePayloads.contains(COLLAPSE))) {
            setExpanded(holder, position == expandedPosition);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    public interface RecordedSongAdapterListener {
        void onItemClick(View holder, File file, int position);

        void onPlayClick(File file);

        void onShareClick(File file);

        void onDeleteClick(File file, int position);
    }

    public static class ViewHolderRecording extends RecyclerView.ViewHolder {

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        @BindView(R.id.item_recording_filename)
        TextView filename;
        @BindView(R.id.item_recording_date)
        TextView date;
        @BindView(R.id.item_recording_duration)
        TextView duration;
        @BindView(R.id.item_recording_action_delete)
        ImageButton btnDelete;
        @BindView(R.id.item_recording_action_share)
        ImageButton btnShare;
        @BindView(R.id.item_recording_action_play)
        ImageButton btnPlay;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolderRecording(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(File item, int position, RecordedSongAdapterListener listener) {
            filename.setText(item.getName());
            date.setText(DateFormat.format("MMM dd, yyyy", item.lastModified()));
            duration.setText(Utils.getDuration(item.getAbsolutePath()));

            itemView.setOnClickListener(v -> listener.onItemClick(itemView, item, position));

            btnPlay.setOnClickListener(v -> listener.onPlayClick(item));
            btnShare.setOnClickListener(v -> listener.onShareClick(item));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(item, position));
        }
    }
}
