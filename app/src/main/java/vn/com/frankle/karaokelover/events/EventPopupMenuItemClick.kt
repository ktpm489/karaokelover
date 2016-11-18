package vn.com.frankle.karaokelover.events

import vn.com.frankle.karaokelover.database.entities.VideoSearchItem

/**
 * Created by duclm on 14-Nov-16.
 */
class EventPopupMenuItemClick(var dataItem: VideoSearchItem, var action: Int) {
    interface ACTION {
        companion object {
            val ADD_FAVORITE = 1
            val REMOVE_FAVORITE = 2
            val ADD_PLAYLIST = 3
        }
    }
}
