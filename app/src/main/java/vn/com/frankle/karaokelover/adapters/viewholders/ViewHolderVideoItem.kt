package vn.com.frankle.karaokelover.adapters.viewholders

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recyclerview_item_video_search.view.*
import vn.com.frankle.karaokelover.KApplication
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import vn.com.frankle.karaokelover.events.EventPopupMenuItemClick

/**
 * Created by duclm on 9/22/2016.
 */

class ViewHolderVideoItem(itemView: View) : ViewHolderBase<VideoSearchItem>(itemView) {

    private var mPopupMenu: PopupMenu? = null
    private lateinit var dataItem: VideoSearchItem

    private fun createPopupMenu(popupMenu: Menu) {
        if (dataItem.isInFavoriteList()) {
            popupMenu.add("Remove from favorite")
                    .setOnMenuItemClickListener { item ->
                        KApplication.eventBus.post(EventPopupMenuItemClick(dataItem, EventPopupMenuItemClick.ACTION.REMOVE_FAVORITE))
                        true
                    }
        } else {
            popupMenu.add("Add to favorite")
                    .setOnMenuItemClickListener { item ->
                        KApplication.eventBus.post(EventPopupMenuItemClick(dataItem, EventPopupMenuItemClick.ACTION.ADD_FAVORITE))
                        true
                    }
        }

        popupMenu.add("Add to playlist")
                .setOnMenuItemClickListener {
                    /**TO-DO-------------------*/
                    true
                }
    }

    override fun bindData(context: Context, dataItem: VideoSearchItem) {
        this.dataItem = dataItem



        itemView.item_search_video_title.text = dataItem.title
        itemView.item_search_play_count.text = dataItem.viewCount
        itemView.item_search_like_count.text = dataItem.likeCount
        itemView.item_search_duration.text = dataItem.duration
        Glide.with(context).load(dataItem.thumbnails)
                .placeholder(R.drawable.drawable_background_default)
                .into(itemView.item_search_video_preview)
        itemView.item_search_more.setOnClickListener { v ->
            mPopupMenu = PopupMenu(v.context, v)
            createPopupMenu(mPopupMenu!!.menu)
            mPopupMenu!!.setOnDismissListener { menu -> mPopupMenu = null }
            mPopupMenu!!.show()
        }
    }

    override fun getViewType(): Int {
        return ViewHolderBase.VIEW_TYPE.DATA_ITEM
    }
}
