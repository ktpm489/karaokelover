package vn.com.frankle.karaokelover.di

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite
import dagger.Module
import dagger.Provides
import vn.com.frankle.karaokelover.database.DbOpenHelper
import vn.com.frankle.karaokelover.database.entities.Favorite
import vn.com.frankle.karaokelover.database.resolvers.FavoriteDeleteResolver
import vn.com.frankle.karaokelover.database.resolvers.FavoriteGetResolver
import vn.com.frankle.karaokelover.database.resolvers.FavoritePutResolver
import javax.inject.Singleton

@Module
class DatabaseModule {

    // We suggest to keep one instance of StorIO (SQLite or ContentResolver)
    // It's thread safe and so on, so just share it.
    // But if you need you can have multiple instances of StorIO
    // (SQLite or ContentResolver) with different settings such as type mapping, logging and so on.
    // But keep in mind that different instances of StorIOSQLite won't share notifications!
    @Provides
    @Singleton
    fun provideStorIOSQLite(sqLiteOpenHelper: SQLiteOpenHelper): StorIOSQLite {
        Log.d("DI", "provideStorIOSQLite")
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(sqLiteOpenHelper)
//                .addTypeMapping(DAOArtist::class.java, DAOArtistSQLit)
                .addTypeMapping(Favorite::class.java, SQLiteTypeMapping.builder<Favorite>()
                        .putResolver(FavoritePutResolver())
                        .getResolver(FavoriteGetResolver())
                        .deleteResolver(FavoriteDeleteResolver())
                        .build())
                .build()
    }

    @Provides
    @Singleton
    fun provideSQLiteOpenHelper(context: Context): SQLiteOpenHelper {
        Log.d("DI", "provideSQLiteOpenHelper")
        return DbOpenHelper(context)
    }
}
