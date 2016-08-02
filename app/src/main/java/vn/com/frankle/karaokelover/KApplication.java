package vn.com.frankle.karaokelover;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;
import vn.com.frankle.karaokelover.database.DbModule;
import vn.com.frankle.karaokelover.services.YoutubeAPIEndpointInterface;

/**
 * Created by duclm on 7/18/2016.
 */

public class KApplication extends Application {

    public static final String BASE_URL = "http://192.168.0.2:8080/";
    public static final String YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/";

    private static Context context;

    public static EventBus eventBus;

    private Retrofit retrofitClient;
    private Retrofit rxYoutubeAPI;

    public static YoutubeAPIEndpointInterface rxYoutubeAPIService;

    @Nullable
    private volatile KAppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        KApplication.context = getApplicationContext();

        eventBus = new EventBus();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofitClient = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        rxYoutubeAPI = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(YOUTUBE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
        rxYoutubeAPIService = rxYoutubeAPI.create(YoutubeAPIEndpointInterface.class);
    }

    @NonNull
    public static KApplication get(@NonNull Context context) {
        return (KApplication) context.getApplicationContext();
    }

    public static YoutubeAPIEndpointInterface getRxYoutubeAPIService() {
        return rxYoutubeAPIService;
    }

    @NonNull
    public KAppComponent appComponent() {
        if (appComponent == null) {
            synchronized (KApplication.class) {
                if (appComponent == null) {
                    appComponent = createAppComponent();
                }
            }
        }

        //noinspection ConstantConditions
        return appComponent;
    }

    @NonNull
    private KAppComponent createAppComponent() {
        return DaggerKAppComponent
                .builder()
                .kAppModule(new KAppModule(this))
                .dbModule(new DbModule())
                .build();
    }
}
