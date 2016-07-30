package vn.com.frankle.karaokelover.database.resolvers;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResolver;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;

import java.util.HashSet;

import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;

public final class ArtistWithKaraokeDeleteResolver extends DeleteResolver<KaraokeAndArtist> {

    @NonNull
    @Override
    public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull KaraokeAndArtist object) {
        return DeleteResult.newInstance(0, new HashSet<String>());
    }
}
