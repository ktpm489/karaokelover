package com.droidcba.kedditbysteps.di

import android.content.Context
import dagger.Module
import dagger.Provides
import vn.com.frankle.karaokelover.KApplication
import javax.inject.Singleton

/**
 *
 * @author juancho.
 */
@Module
class AppModule(val app: KApplication) {

    @Provides
    @Singleton
    fun provideContext(): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideApplication(): KApplication {
        return app
    }
}