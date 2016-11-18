package vn.com.frankle.karaokelover.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderBase
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderVideoItem
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import java.util.*

/**
 * Created by duclm on 9/22/2016.
 */

class KAdapterYoutubeVideoSearch : RecyclerViewEndlessScrollBaseAdapter<VideoSearchItem> {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, onItemClickListener: RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem>) : super(context, onItemClickListener) {
    }

    constructor(context: Context, data: ArrayList<VideoSearchItem>) : super(context, data) {
    }

    public override fun createView(parent: ViewGroup): ViewHolderVideoItem {
        val layoutInflater = LayoutInflater.from(mContext)
        val videoItemView = layoutInflater.inflate(R.layout.recyclerview_item_video_search, parent, false)
        return ViewHolderVideoItem(videoItemView)
    }

    override fun bindView(item: VideoSearchItem, viewHolder: ViewHolderBase<VideoSearchItem>) {
        if (viewHolder is ViewHolderVideoItem) {
            viewHolder.bindData(mContext, item)
        }
    }
}
