package vn.com.frankle.karaokelover.database.resolvers

import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import vn.com.frankle.karaokelover.database.entities.Favorite

/**
 * Created by duclm on 14-Nov-16.
 */
class FavoriteDeleteResolver : DefaultDeleteResolver<Favorite>() {
    override fun mapToDeleteQuery(`object`: Favorite): DeleteQuery {
        return DeleteQuery.builder()
                .table("favorites")
                .where("_id = ?")
                .whereArgs(`object`.id)
                .build()
    }
}