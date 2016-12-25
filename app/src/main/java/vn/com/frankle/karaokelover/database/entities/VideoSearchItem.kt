package vn.com.frankle.karaokelover.database.entities

import io.realm.Realm
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm
import vn.com.frankle.karaokelover.database.tables.FavoriteTable
import vn.com.frankle.karaokelover.models.Thumbnails
import vn.com.frankle.karaokelover.util.Utils

/**
 * Created by duclm on 8/2/2016.
 */

class VideoSearchItem(var videoId: String?, var title: String?, duration: String?, viewcount: String?, likecount: String?, private var mThumbnails: Thumbnails?) : Comparable<VideoSearchItem> {

    var duration: String? = null
        get() = Utils.convertYoutubeTimeformat(field)
    var viewCount: String? = null
        get() = Utils.getViewCount(field)
    var likeCount: String? = null
        get() = Utils.getLikeCount(field)
    private var isInFavoriteList: Boolean = false

    init {
        this.duration = duration
        this.viewCount = viewcount
        this.likeCount = likecount
    }

    val thumbnails: String
        get() = Utils.getThumbnailURL(this.mThumbnails)

    fun setThumbnails(mThumbnails: Thumbnails) {
        this.mThumbnails = mThumbnails
    }

    fun isInFavoriteList(): Boolean {
        val inserted = Realm.getDefaultInstance().where(FavoriteRealm::class.java).equalTo(FavoriteTable.COLUMN_VIDEO_ID, videoId).findFirst()
        isInFavoriteList = inserted != null
        return isInFavoriteList
    }


    override fun compareTo(other: VideoSearchItem): Int {
        if (this.videoId === other.videoId) {
            return 0
        }
        return -1
    }
}
