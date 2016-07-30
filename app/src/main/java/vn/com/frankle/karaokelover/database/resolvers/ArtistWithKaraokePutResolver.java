package vn.com.frankle.karaokelover.database.resolvers;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.KaraokeTable;

public final class ArtistWithKaraokePutResolver extends PutResolver<KaraokeAndArtist> {

    @Override
    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull KaraokeAndArtist karaokeAndArtist) {
        return PutResult.newUpdateResult(0, new HashSet<String>());
    }
}
