package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 7/17/2016.
 */

public class KHostArtistWithSongsAdapter extends RecyclerView.Adapter<KHostArtistWithSongsAdapter.ViewHolder> {

    private Context mContext;
    private List<VideoSearchItem> mSearchResult;


    public KHostArtistWithSongsAdapter(Context context) {
        mContext = context;
        mSearchResult = new ArrayList<>();
    }

    public KHostArtistWithSongsAdapter(Context context, List<VideoSearchItem> searchResult) {
        mContext = context;
        this.mSearchResult = searchResult;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate the custom layout
        View hotKaraokeView = inflater.inflate(R.layout.item_search, parent, false);

        // Return a new holder instance
        return new ViewHolder(hotKaraokeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoSearchItem itemVideo = mSearchResult.get(position);

        // Set item views based on your views and data model
        holder.title.setText(itemVideo.getTitle());
        holder.playCount.setText(itemVideo.getViewCount());
        holder.likeCount.setText(itemVideo.getLikeCount());
        holder.duration.setText(itemVideo.getDuration());
        Glide.with(mContext).load(itemVideo.getThumbnails()).into(holder.preview);
    }

    @Override
    public int getItemCount() {
        return mSearchResult.size();
    }

    /**
     * Populate adapter with new data set
     *
     * @param searchData : new data set
     */
    public void populateWithData(List<VideoSearchItem> searchData) {
        this.mSearchResult = searchData;
        notifyDataSetChanged();
    }

    /**
     * Remove all item from current data set
     */
    public void clearData() {
        if (this.mSearchResult != null && this.mSearchResult.size() > 0) {
            this.mSearchResult.clear();
            notifyDataSetChanged();
        }
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        @BindView(R.id.item_search_video_title)
        TextView title;
        @BindView(R.id.item_search_play_count)
        TextView playCount;
        @BindView(R.id.item_search_like_count)
        TextView likeCount;
        @BindView(R.id.item_search_video_preview)
        ImageView preview;
        @BindView(R.id.item_search_duration)
        TextView duration;
        @BindView(R.id.item_search_more)
        ImageButton btnMore;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

}
