package vn.com.frankle.karaokelover.presenters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.views.ViewHolderHotArtist;

/**
 * Created by duclm on 7/19/2016.
 */

public class KHotArtistAdapter extends RecyclerView.Adapter<ViewHolderHotArtist> {

    private Context mContext;
    private List<ArtistWithKaraoke> mHotArtists;
    private KHotKaraokeRecycleAdapter mHotKaraokes;

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
        holder.getArtistNameTextview().setText(artist.getArtist());

        mHotKaraokes = new KHotKaraokeRecycleAdapter(mContext, artist);
        holder.getHotSongsRecycleView().setAdapter(mHotKaraokes);
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
}
