package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.util.Utils;

/**
 * Created by duclm on 9/7/2016.
 */

public class KAdapterMyRecordings extends RecyclerView.Adapter<KAdapterMyRecordings.ViewHolderRecording> {

    public interface OnItemClickListener {
        void onItemClick(File file);
    }

    private Context mContext;
    private File[] mRecordings;

    private OnItemClickListener mListener;


    public KAdapterMyRecordings(Context context, File[] listRecordings, OnItemClickListener listener) {
        this.mContext = context;
        this.mRecordings = listRecordings;
        this.mListener = listener;
    }


    @Override
    public ViewHolderRecording onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View recordingItem = inflater.inflate(R.layout.item_my_recording, parent, false);
        return new ViewHolderRecording(mContext, recordingItem);
    }

    @Override
    public void onBindViewHolder(ViewHolderRecording holder, int position) {
        File item = mRecordings[position];

        holder.bind(item, mListener);
    }

    @Override
    public int getItemCount() {
        if (mRecordings == null) {
            return 0;
        }
        return mRecordings.length;
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

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolderRecording(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(File item, OnItemClickListener listener) {
            filename.setText(item.getName());
            date.setText(DateFormat.format("MMM dd, yyyy", item.lastModified()));
            duration.setText(Utils.getDuration(item.getAbsolutePath()));

            itemView.setOnClickListener(view -> listener.onItemClick(item));
        }
    }
}
