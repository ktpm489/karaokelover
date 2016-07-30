package vn.com.frankle.karaokelover.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.Utils;

/**
 * Created by duclm on 7/19/2016.
 */

public class ViewHolderHotArtist extends RecyclerView.ViewHolder {

    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    @BindView(R.id.tv_hot_artist_name)
    TextView artistName;
    @BindView(R.id.recycleview_hot_artist_songs)
    RecyclerView hotSongs;

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

    public TextView getArtistNameTextview() {
        return artistName;
    }

    public RecyclerView getHotSongsRecycleView() {
        return hotSongs;
    }
}
