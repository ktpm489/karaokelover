package vn.com.frankle.karaokelover.adapters.viewholders;

import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 9/22/2016.
 */

public class ViewHolderVideoItem extends ViewHolderBase<VideoSearchItem> {
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

    private PopupMenu mPopupMenu;

    public ViewHolderVideoItem(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    private void createPopupMenu(Menu popupMenu) {
        popupMenu.add("Add to favorites")
                .setOnMenuItemClickListener(item -> {
                    Log.d("LOADMORE", "Click on context menu");
                    return true;
                });
    }

    @Override
    public void bindData(Context context, VideoSearchItem dataItem) {
        title.setText(dataItem.getTitle());
        playCount.setText(dataItem.getViewCount());
        likeCount.setText(dataItem.getLikeCount());
        duration.setText(dataItem.getDuration());
        Glide.with(context).load(dataItem.getThumbnails())
                .placeholder(R.drawable.drawable_background_default).placeholder(R.drawable.drawable_background_default).into(preview);
        btnMore.setOnClickListener(v -> {
            mPopupMenu = new PopupMenu(v.getContext(), v);
            createPopupMenu(mPopupMenu.getMenu());
            mPopupMenu.setOnDismissListener(menu -> mPopupMenu = null);
            mPopupMenu.show();
        });
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE.DATA_ITEM;
    }
}
