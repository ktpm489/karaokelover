package vn.com.frankle.karaokelover.database.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Created by duclm on 7/24/2016.
 */

public class ArtistTable {

    // This is just class with Meta Data, we don't need instances
    private ArtistTable() {
        throw new IllegalStateException("No instances please");
    }

    @NonNull
    public static final String TABLE = "artist";

    @NonNull
    public static final String COLUMN_ID = "id";

    @NonNull
    public static final String COLUMN_NAME = "name";

    @NonNull
    public static final String COLUMN_ISVIETNAMESE = "is_vietnamese";

    // Yep, with StorIO you can safely store queries as objects and reuse them, they are immutable
    @NonNull
    public static final Query QUERY_ALL = Query.builder()
            .table(TABLE)
            .build();
}
