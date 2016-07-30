package vn.com.frankle.karaokelover.database.resolvers;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio.sqlite.operations.get.GetResolver;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOKaraoke;
import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.KaraokeTable;

/**
 * Created by duclm on 7/24/2016.
 */

public final class ArtistWithKaraokeGetResolver extends DefaultGetResolver<KaraokeAndArtist> {

        // We expect that cursor will contain both Tweet and User: SQL JOIN
        @NonNull
        @Override
        public KaraokeAndArtist mapFromCursor(@NonNull Cursor cursor) {
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(ArtistTable.COLUMN_NAME));

            String video_id = cursor.getString(cursor.getColumnIndexOrThrow(KaraokeTable.COLUMN_VIDEOID));

            return new KaraokeAndArtist(artist, video_id);
        }
}
