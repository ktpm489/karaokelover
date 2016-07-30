package vn.com.frankle.karaokelover.database;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrend;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.HotTrendTable;

/**
 * Created by duclm on 7/24/2016.
 */

public class DatabaseQueryHelper {
    @NonNull
    private final StorIOSQLite storIOSQLite;

    public DatabaseQueryHelper(@NonNull StorIOSQLite storIOSQLite) {
        this.storIOSQLite = storIOSQLite;
    }

    @NonNull
    public List<DAOArtist> getHotArtist() {
        return storIOSQLite
                .get()
                .listOfObjects(DAOArtist.class)
                .withQuery(RawQuery.builder()
                        .query("SELECT * FROM "
                                + ArtistTable.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @NonNull
    public List<DAOHotTrend> getHotTrends() {
        return storIOSQLite
                .get()
                .listOfObjects(DAOHotTrend.class)
                .withQuery(RawQuery.builder()
                        .query("SELECT * FROM "
                                + HotTrendTable.TABLE)
                        .build())
                .prepare()
                .executeAsBlocking();
    }
}
