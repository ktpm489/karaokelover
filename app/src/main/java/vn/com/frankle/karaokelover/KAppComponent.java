package vn.com.frankle.karaokelover;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.frankle.karaokelover.database.DbModule;

@Singleton
@Component(
        modules = {
                KAppModule.class,
                DbModule.class
        }
)
public interface KAppComponent {
    void inject(@NonNull KActivity_home activity_home);

    @NonNull
    StorIOSQLite storIOSQLite();
}
