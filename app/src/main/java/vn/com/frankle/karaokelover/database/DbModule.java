package vn.com.frankle.karaokelover.database;

import android.content.Context;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOArtistSQLiteTypeMapping;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrend;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrendSQLiteTypeMapping;
import vn.com.frankle.karaokelover.database.entities.DAOKaraoke;
import vn.com.frankle.karaokelover.database.entities.DAOKaraokeSQLiteTypeMapping;
import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;
import vn.com.frankle.karaokelover.database.resolvers.ArtistWithKaraokeDeleteResolver;
import vn.com.frankle.karaokelover.database.resolvers.ArtistWithKaraokeGetResolver;
import vn.com.frankle.karaokelover.database.resolvers.ArtistWithKaraokePutResolver;

@Module
public class DbModule {

    // We suggest to keep one instance of StorIO (SQLite or ContentResolver)
    // It's thread safe and so on, so just share it.
    // But if you need you can have multiple instances of StorIO
    // (SQLite or ContentResolver) with different settings such as type mapping, logging and so on.
    // But keep in mind that different instances of StorIOSQLite won't share notifications!
    @Provides
    @NonNull
    @Singleton
    public StorIOSQLite provideStorIOSQLite(@NonNull SQLiteAssetHelper sqLiteOpenHelper) {
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(sqLiteOpenHelper)
                .addTypeMapping(DAOArtist.class, new DAOArtistSQLiteTypeMapping())
                .addTypeMapping(DAOKaraoke.class, new DAOKaraokeSQLiteTypeMapping())
                .addTypeMapping(DAOHotTrend.class, new DAOHotTrendSQLiteTypeMapping())
                .addTypeMapping(KaraokeAndArtist.class, SQLiteTypeMapping.<KaraokeAndArtist>builder()
                        .putResolver(new ArtistWithKaraokePutResolver())
                        .getResolver(new ArtistWithKaraokeGetResolver())
                        .deleteResolver(new ArtistWithKaraokeDeleteResolver())
                        .build())
                .build();
    }

    @Provides
    @NonNull
    @Singleton
    public SQLiteAssetHelper provideSQLiteOpenHelper(@NonNull Context context) {
        return new DbOpenHelper(context);
    }
}
