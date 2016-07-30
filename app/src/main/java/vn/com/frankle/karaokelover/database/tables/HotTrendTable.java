package vn.com.frankle.karaokelover.database.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Created by duclm on 7/28/2016.
 */

public class HotTrendTable {
    // This is just class with Meta Data, we don't need instances
    private HotTrendTable() {
        throw new IllegalStateException("No instances please");
    }

    @NonNull
    public static final String TABLE = "hot_trend";

    @NonNull
    public static final String COLUMN_ID = "id";

    @NonNull
    public static final String COLUMN_VIDEOID = "video_id";

    // Yep, with StorIO you can safely store queries as objects and reuse them, they are immutable
    @NonNull
    public static final Query QUERY_ALL = Query.builder()
            .table(TABLE)
            .build();
}
