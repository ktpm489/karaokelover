package vn.com.frankle.karaokelover.database.resolvers

import android.database.Cursor
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import vn.com.frankle.karaokelover.database.entities.Favorite

/**
 * Created by duclm on 14-Nov-16.
 */
class FavoriteGetResolver : DefaultGetResolver<Favorite>() {

    override fun mapFromCursor(cursor: Cursor): Favorite {
        val `object` = Favorite()

        if (!cursor.isNull(cursor.getColumnIndex("collection_id"))) {
            `object`.collection_id = cursor.getInt(cursor.getColumnIndex("collection_id"))
        }
        if (!cursor.isNull(cursor.getColumnIndex("_id"))) {
            `object`.id = cursor.getLong(cursor.getColumnIndex("_id"))
        }
        `object`.video_id = cursor.getString(cursor.getColumnIndex("video_id"))

        return `object`
    }
}