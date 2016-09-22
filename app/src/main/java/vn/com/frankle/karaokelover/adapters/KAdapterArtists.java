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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtist;
import vn.com.frankle.karaokelover.zingmp3.ZingMp3API;

/**
 * Created by duclm on 9/19/2016.
 */

public class KAdapterArtists extends RecyclerView.Adapter<KAdapterArtists.ViewHolderArtist> {
    private static final String DEBUG_TAG = KAdapterArtists.class.getSimpleName();

    public interface OnItemClickListener {
        void onArtistClick(ZingArtist artist);
    }

    private Context mContext;
    private List<ZingArtist> mArtistsList;

    private KAdapterArtists.OnItemClickListener mListener;

    public KAdapterArtists(Context context, OnItemClickListener listener) {
        mContext = context;
        this.mArtistsList = null;
        this.mListener = listener;
    }

    public KAdapterArtists(Context context, @NonNull List<ZingArtist> artistList) {
        mContext = context;
        this.mArtistsList = artistList;
    }

    @Override
    public KAdapterArtists.ViewHolderArtist onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View artistItem = inflater.inflate(R.layout.recyclerview_item_artist_zing, parent, false);

        // Return a new holder instance
        return new KAdapterArtists.ViewHolderArtist(mContext, artistItem);
    }


    @Override
    public void onBindViewHolder(KAdapterArtists.ViewHolderArtist holder, int position) {
        ZingArtist itemArtist = mArtistsList.get(position);

        // Set item views based on your views and data model
        holder.bind(mContext, itemArtist, mListener);
    }

    @Override
    public int getItemCount() {
        if (mArtistsList != null) {
            return mArtistsList.size();
        }
        return 0;
    }

    public void populateWithData(List<ZingArtist> mArtistsList) {
        this.mArtistsList = mArtistsList;
        notifyDataSetChanged();
    }

    static class ViewHolderArtist extends RecyclerView.ViewHolder {

        @BindView(R.id.list_artist_avatar)
        ImageView mAvatar;
        @BindView(R.id.list_artist_name)
        TextView mArtistName;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolderArtist(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(final Context context, final ZingArtist artist, final OnItemClickListener listener) {

            mArtistName.setText(artist.getName());

            Glide.with(context).load(ZingMp3API.getArtistAvatarURL(artist.getAvatar())).into(mAvatar);
            itemView.setOnClickListener(v -> listener.onArtistClick(artist));
        }
    }
}
