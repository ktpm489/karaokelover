package vn.com.frankle.karaokelover.database.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Created by duclm on 14-Nov-16.
 */

public class FavoriteTable {
    @NonNull
    public static final String TABLE = "favorites";

    @NonNull
    public static final String COLUMN_ID = "_id";

    /**
     * Video Id (get from Youtube server)
     */
    @NonNull
    public static final String COLUMN_VIDEO_ID = "video_id";

    /**
     * Collection id (get from collection table)
     */
    @NonNull
    public static final String COLUMN_COLLECTION_ID = "collection_id";

    // Yep, with StorIO you can safely store queries as objects and reuse them, they are immutable
    @NonNull
    public static final Query QUERY_ALL = Query.builder()
            .table(TABLE)
            .build();

    // Yep, with StorIO you can safely store queries as objects and reuse them, they are immutable
    @NonNull
    public static final Query QUERY_ALL_DISTINCT = Query.builder()
            .table(TABLE)
            .columns(COLUMN_VIDEO_ID)
            .distinct(true)
            .build();

    @NonNull
    public static String getCreateTableQuery() {
        return "CREATE TABLE " + TABLE + "("
                + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, "
                + COLUMN_VIDEO_ID + " TEXT NOT NULL, "
                + COLUMN_COLLECTION_ID + " INTEGER NOT NULL"
                + ");";
    }
}
