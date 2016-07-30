package vn.com.frankle.karaokelover.database.tables;

import android.support.annotation.NonNull;

/**
 * Created by duclm on 7/24/2016.
 */

public class KaraokeTable {

    // This is just class with Meta Data, we don't need instances
    private KaraokeTable() {
        throw new IllegalStateException("No instances please");
    }

    @NonNull
    public static final String TABLE = "karaoke";

    @NonNull
    public static final String COLUMN_ID = "id";

    @NonNull
    public static final String COLUMN_ARTIST = "artist";

    @NonNull
    public static final String COLUMN_VIDEOID = "video_id";
}
