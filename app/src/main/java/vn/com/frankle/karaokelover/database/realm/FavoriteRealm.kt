package vn.com.frankle.karaokelover.database.realm

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

/**
 * Created by duclm on 17-Nov-16.
 */
open class FavoriteRealm(
        @PrimaryKey open var id: Long = 0,
        @Index var video_id: String? = null
) : RealmObject() {
    companion object Column {
        val COLUMN_VIDEO_ID = "video_id"
    }
}