package vn.com.frankle.karaokelover.adapters;

/**
 * Created by duclm on 9/22/2016.
 */

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
import rx.Observable;
import rx.subjects.PublishSubject;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 7/17/2016.
 */

public class KAdapterYoutbeVideoSearchLimit extends RecyclerView.Adapter<KAdapterYoutbeVideoSearchLimit.ViewHolder> {

    private final Context mContext;
    private final PublishSubject<VideoSearchItem> rxOnClickSubject = PublishSubject.create();
    private List<VideoSearchItem> mSearchResult;

    public KAdapterYoutbeVideoSearchLimit(Context context) {
        mContext = context;
        mSearchResult = new ArrayList<>();
    }


    public KAdapterYoutbeVideoSearchLimit(Context context, List<VideoSearchItem> searchResult) {
        mContext = context;
        this.mSearchResult = searchResult;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate the custom layout
        View hotKaraokeView = inflater.inflate(R.layout.recyclerview_item_video_search, parent, false);

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
        Glide.with(mContext).load(itemVideo.getThumbnails())
                .placeholder(R.drawable.drawable_default_preview).into(holder.preview);

        holder.itemView.setOnClickListener(view -> {
            rxOnClickSubject.onNext(itemVideo);
        });
    }

    /**
     * Get on item click listener observable
     *
     * @return OnItemClick event observable
     */
    public Observable<VideoSearchItem> getItemClickListener() {
        return rxOnClickSubject.asObservable();
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
    public void addDataItemList(List<VideoSearchItem> searchData) {
        this.mSearchResult.addAll(searchData);
        notifyDataSetChanged();
    }

    /**
     * Remove a video from list
     *
     * @param videoId : id of video to be removed
     */
    public void removeVideoFromList(String videoId) {
        for (VideoSearchItem video : mSearchResult) {
            if (video.getVideoId().equals(videoId)) {
                mSearchResult.remove(video);
                notifyDataSetChanged();
                break;
            }
        }
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

    public interface OnItemClickListener {
        void onItemClick(VideoSearchItem item);
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

        public void bind(final Context context, final VideoSearchItem item) {
            title.setText(item.getTitle());
            playCount.setText(item.getViewCount());
            likeCount.setText(item.getLikeCount());
            duration.setText(item.getDuration());
            Glide.with(context).load(item.getThumbnails()).into(preview);
        }
    }

}