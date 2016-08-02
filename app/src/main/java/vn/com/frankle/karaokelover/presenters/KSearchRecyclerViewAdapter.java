package vn.com.frankle.karaokelover.presenters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.Utils;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSearch;
import vn.com.frankle.karaokelover.models.Snippet;

/**
 * Created by duclm on 7/17/2016.
 */

public class KSearchRecyclerViewAdapter extends RecyclerView.Adapter<KSearchRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private ResponseYoutubeSearch mSearchResult;


    public KSearchRecyclerViewAdapter(Context context) {
        mContext = context;
        mSearchResult = new ResponseYoutubeSearch();
    }

    public KSearchRecyclerViewAdapter(Context context, ResponseYoutubeSearch searchResult) {
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
        Snippet youtubeKaraoke = mSearchResult.getItems().get(position).getSnippet();

        // Set item views based on your views and data model
        holder.title.setText(youtubeKaraoke.getTitle());
        Glide.with(mContext).load(Utils.getThumbnailURL(youtubeKaraoke.getThumbnails())).into(holder.preview);
    }

    @Override
    public int getItemCount() {
        return mSearchResult.getItems().size();
    }

    public void populateWithData(ResponseYoutubeSearch searchData) {
        this.mSearchResult = searchData;
        notifyDataSetChanged();
    }

    public void clearData() {
        if (this.mSearchResult != null && this.mSearchResult.getItems().size() > 0) {
            this.mSearchResult.getItems().clear();
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
        @BindView(R.id.item_search_video_preview)
        ImageView preview;
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
