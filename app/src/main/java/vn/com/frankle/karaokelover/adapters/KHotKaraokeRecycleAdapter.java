package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 7/17/2016.
 */

public class KHotKaraokeRecycleAdapter extends RecyclerView.Adapter<KHotKaraokeRecycleAdapter.ViewHolder> {

    private final OnItemClickListener mListener;
    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();
    private Context mContext;
    private List<VideoSearchItem> mYoutubeKaraokes;

    public KHotKaraokeRecycleAdapter(Context context, ArtistWithKaraoke artistWithKaraoke, OnItemClickListener onItemClickListener) {
        mContext = context;
        mYoutubeKaraokes = new ArrayList<>();
        mYoutubeKaraokes = artistWithKaraoke.getVideoList();
        mListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate the custom layout
        View hotKaraokeView = inflater.inflate(R.layout.item_hot_artist_song, parent, false);

        // Return a new holder instance
        return new ViewHolder(hotKaraokeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoSearchItem videoItem = mYoutubeKaraokes.get(position);

        // Set item views based on your views and data model
        holder.bind(mContext, videoItem, mListener);
    }

    @Override
    public int getItemCount() {
        return mYoutubeKaraokes.size();
    }

    public interface OnItemClickListener {
        void onItemClick(VideoSearchItem item);
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        @BindView(R.id.tv_item_hot_karaoke_title)
        TextView title;
        @BindView(R.id.imgv_item_hot_karaoke_item_preview)
        ImageView preview;
        @BindView(R.id.tv_item_hotkaraoke_duration)
        TextView duration;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

//        public void bind(final Context context, final ResponseSnippetContentDetails item, final OnItemClickListener listener) {
//            title.setText(item.getItems().get(0).getSnippet().getTitle());
//            duration.setText(Utils.convertYoutubeTimeformat(item.getItems().get(0).getContentDetails().getDuration()));
//            Glide.with(context).load(Utils.getThumbnailURL(item.getItems().get(0).getSnippet().getThumbnails()))
//                    .placeholder(R.drawable.drawable_default_preview).into(preview);
//            itemView.setOnClickListener(v -> listener.onItemClick(item));
//        }

        public void bind(final Context context, final VideoSearchItem item, final OnItemClickListener listener) {
            title.setText(item.getTitle());
            duration.setText(item.getDuration());
            Glide.with(context).load(item.getThumbnails())
                    .placeholder(R.drawable.drawable_default_preview).into(preview);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
