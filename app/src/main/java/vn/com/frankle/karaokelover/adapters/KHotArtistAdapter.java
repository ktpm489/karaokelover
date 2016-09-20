package vn.com.frankle.karaokelover.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.KActivityArtistDetails;
import vn.com.frankle.karaokelover.KActivityHome;
import vn.com.frankle.karaokelover.KActivityPlayVideo;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.fragments.KFragmentFavorite;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetContentDetails;
import vn.com.frankle.karaokelover.util.Utils;
import vn.com.frankle.karaokelover.views.SpaceItemDecoration;

/**
 * Created by duclm on 7/19/2016.
 */

public class KHotArtistAdapter extends RecyclerView.Adapter<KHotArtistAdapter.ViewHolderHotArtist> {

    private Context mContext;
    private List<ArtistWithKaraoke> mHotArtists;
    private KHotKaraokeRecycleAdapter mHotKaraokes;

    private final KHotKaraokeRecycleAdapter.OnItemClickListener mListener = this::handleOnVideoClickListener;

    private void handleOnVideoClickListener(ResponseSnippetContentDetails item) {
        Intent playVideoItent = new Intent(mContext, KActivityPlayVideo.class);
        playVideoItent.putExtra("title", item.getItems().get(0).getSnippet().getTitle());
        playVideoItent.putExtra("videoid", item.getItems().get(0).getId());
        ((KActivityHome) mContext).startActivityForResult(playVideoItent, KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST);
    }

    public KHotArtistAdapter(Context context) {
        mContext = context;
        mHotArtists = new ArrayList<>();
    }

    public KHotArtistAdapter(Context context, @NonNull List<ArtistWithKaraoke> hotArtists) {
        mContext = context;
        mHotArtists = hotArtists;
    }

    @Override
    public ViewHolderHotArtist onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View hotArtistView = inflater.inflate(R.layout.item_hot_artist, parent, false);

        // Return a new holder instance
        return new ViewHolderHotArtist(mContext, hotArtistView);
    }


    @Override
    public void onBindViewHolder(ViewHolderHotArtist holder, int position) {
        ArtistWithKaraoke artist = mHotArtists.get(position);

        // Set item views based on your views and data model
        holder.artistName.setText(artist.getArtist());
        // Add action listener for MORE button
        holder.btnMore.setOnClickListener(view -> {
            Intent artistDetails = new Intent(mContext, KActivityArtistDetails.class);
            Bundle args = new Bundle();
            args.putString("artist", artist.getArtist());
            artistDetails.putExtras(args);
            mContext.startActivity(artistDetails);
        });
        mHotKaraokes = new KHotKaraokeRecycleAdapter(mContext, artist, mListener);
        holder.hotSongs.setAdapter(mHotKaraokes);
    }

    @Override
    public int getItemCount() {
        if (mHotArtists != null) {
            return mHotArtists.size();
        }
        return 0;
    }

    public void updateAdapterData(List<ArtistWithKaraoke> newData) {
        mHotArtists = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolderHotArtist extends RecyclerView.ViewHolder {

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        @BindView(R.id.tv_hot_artist_name)
        TextView artistName;
        @BindView(R.id.recycleview_hot_artist_songs)
        RecyclerView hotSongs;
        @BindView(R.id.btn_more_artist)
        TextView btnMore;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolderHotArtist(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            hotSongs.setHasFixedSize(true);
            hotSongs.setLayoutManager(layoutManager);
            hotSongs.addItemDecoration(new SpaceItemDecoration(Utils.convertDpToPixel(context, 16), SpaceItemDecoration.HORIZONTAL));
            hotSongs.setNestedScrollingEnabled(false);
        }
    }

}
