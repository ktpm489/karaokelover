package vn.com.frankle.karaokelover;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Singleton;

import dagger.Component;
import vn.com.frankle.karaokelover.database.DbModule;
import vn.com.frankle.karaokelover.fragments.KFragmentHome;

@Singleton
@Component(
        modules = {
                KAppModule.class,
                DbModule.class
        }
)
public interface KAppComponent {
    void inject(@NonNull KFragmentHome fragmentHome);

    @NonNull
    StorIOSQLite storIOSQLite();
}
