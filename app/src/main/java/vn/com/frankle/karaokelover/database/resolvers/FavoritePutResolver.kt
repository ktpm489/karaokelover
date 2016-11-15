package vn.com.frankle.karaokelover.database.resolvers

import android.content.ContentValues
import android.util.Log
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.operations.put.PutResult
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import vn.com.frankle.karaokelover.database.entities.Favorite
import vn.com.frankle.karaokelover.database.tables.FavoriteTable

/**
 * Created by duclm on 14-Nov-16.
 */
class FavoritePutResolver : DefaultPutResolver<Favorite>() {
    override fun mapToContentValues(`object`: Favorite): ContentValues {
        val contentValues = ContentValues(3)

        contentValues.put(FavoriteTable.COLUMN_COLLECTION_ID, `object`.collection_id)
        contentValues.put(FavoriteTable.COLUMN_ID, `object`.id)
        contentValues.put(FavoriteTable.COLUMN_VIDEO_ID, `object`.video_id)

        return contentValues
    }

    override fun mapToInsertQuery(`object`: Favorite): InsertQuery {
        return InsertQuery.builder()
                .table(FavoriteTable.TABLE)
                .build()
    }

    override fun mapToUpdateQuery(`object`: Favorite): UpdateQuery {
        return UpdateQuery.builder()
                .table(FavoriteTable.TABLE)
                .where("_id = ?")
                .whereArgs(`object`.id)
                .build()
    }

    override fun performPut(storIOSQLite: StorIOSQLite, `object`: Favorite): PutResult {
        Log.d("FAVORITE", "Performing put action to db")
        return super.performPut(storIOSQLite, `object`)
    }
}