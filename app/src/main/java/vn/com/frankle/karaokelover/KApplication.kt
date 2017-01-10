package vn.com.frankle.karaokelover

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Environment
import android.preference.PreferenceManager
import com.droidcba.kedditbysteps.di.AppModule
import com.google.firebase.crash.FirebaseCrash
import io.realm.Realm
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.schedulers.Schedulers
import vn.com.frankle.karaokelover.activities.KActivityHome
import vn.com.frankle.karaokelover.activities.KActivitySettings
import vn.com.frankle.karaokelover.di.DaggerKAppComponent
import vn.com.frankle.karaokelover.di.DatabaseModule
import vn.com.frankle.karaokelover.di.KAppComponent
import vn.com.frankle.karaokelover.services.YoutubeAPIEndpointInterface
import vn.com.frankle.karaokelover.services.YoutubeAudioMp3APIInterface
import vn.com.frankle.karaokelover.services.ZingMp3APIEndpointInterface
import vn.com.frankle.karaokelover.util.Utils
import java.util.concurrent.TimeUnit


/**
 * Created by duclm on 7/18/2016.
 */

class KApplication : Application() {

    private var rxYoutubeAPI: Retrofit? = null
    private var rxAudioMP3API: Retrofit? = null
    private var rxZingMp3API: Retrofit? = null
    private val youtubeInMp3: Retrofit? = null

    private val mUncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
        // Report crash to firebase
        FirebaseCrash.report(throwable)

        // Restart application
        val restartIntent = packageManager.getLaunchIntentForPackage("vn.com.frankle.karaokelover")
        restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        restartIntent.putExtra(KActivityHome.INTENT_EXTRA_RECOVER, true)
        startActivity(restartIntent)

        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }

    lateinit var appComponent: KAppComponent

    override fun onCreate() {
        super.onCreate()

        KApplication.context = applicationContext

        appResource = resources

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        val strValue = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(KActivitySettings.SettingsFragment.KEY_PREF_PREVIEW_IMG_QUALITY, "")
        if (strValue == "0") {
            HD_PREVIEW_VIDEO_FLAG = false
        } else {
            HD_PREVIEW_VIDEO_FLAG = true
        }

        Realm.init(this@KApplication)

        appComponent = DaggerKAppComponent.builder()
                .appModule(AppModule(this))
                .databaseModule(DatabaseModule()).build()

        eventBus = EventBus()

        val builder = OkHttpClient().newBuilder()
        builder.readTimeout(10, TimeUnit.SECONDS)
        builder.connectTimeout(10, TimeUnit.SECONDS)
        builder.cache(Cache(this.cacheDir, 10 * 10 * 1024))
                .addInterceptor { chain ->
                    var request = chain.request()
                    if (Utils.isOnline(applicationContext)) {
                        request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build()
                    } else {
                        request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                    }
                    chain.proceed(request)
                }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        builder.addInterceptor(interceptor)
        val client = builder.build()

        rxYoutubeAPI = Retrofit.Builder()
                .client(client)
                .baseUrl(YOUTUBE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        rxYoutubeAPIService = rxYoutubeAPI!!.create(YoutubeAPIEndpointInterface::class.java)

        rxZingMp3API = Retrofit.Builder()
                .client(client)
                .baseUrl(ZING_MP3_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        rxZingMp3APIService = rxZingMp3API!!.create(ZingMp3APIEndpointInterface::class.java)

        rxAudioMP3API = Retrofit.Builder()
                .client(client)
                .baseUrl(YOUTUBE_MP3_AUDIO_URL)
                .build()
        youtubeInMp3APIService = rxAudioMP3API!!.create(YoutubeAudioMp3APIInterface::class.java)

        // Handle uncaught crash
        Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler)
    }

//    fun appComponent(): KAppComponent {
//        if (appComponent == null) {
//            synchronized(KApplication::class.java) {
//                if (appComponent == null) {
//                    appComponent = createAppComponent()
//                }
//            }
//        }
//
//
//        return appComponent
//    }

    companion object {

        val BASE_URL = "http://192.168.0.2:8080/"
        val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
        val YOUTUBE_MP3_AUDIO_URL = "http://www.youtubeinmp3.com/"
        val ZING_MP3_BASE_URL = "http://api.mp3.zing.vn/api/"
        val RECORDING_DIRECTORY_URI = Environment.getExternalStorageDirectory().absolutePath + "/Karaoke Lover/"
        val TAG_DEFAULT = KApplication::class.java.simpleName

        private var context: Context? = null

        lateinit var eventBus: EventBus
        var HD_PREVIEW_VIDEO_FLAG: Boolean = false

        lateinit var rxYoutubeAPIService: YoutubeAPIEndpointInterface
        lateinit var youtubeInMp3APIService: YoutubeAudioMp3APIInterface
        lateinit var rxZingMp3APIService: ZingMp3APIEndpointInterface
        lateinit var appResource: Resources

        operator fun get(context: Context): KApplication {
            return context.applicationContext as KApplication
        }
    }
}
