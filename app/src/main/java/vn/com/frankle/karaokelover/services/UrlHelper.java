package vn.com.frankle.karaokelover.services;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by duclm on 8/16/2016.
 */

public class UrlHelper {
    private static final String TAG = UrlHelper.class.getSimpleName();

    public static String getYoutubeInMp3Url(@NonNull String videoId) {
        String url = String.format(
                "http://www.youtubeinmp3.com/download/?" +
                        "video=https://www.youtube.com/watch?v=%s" +
                        "&autostart=1",
                videoId
        );
        Log.d(TAG, "YoutubeInMp3Url = " + url);
        return url;
    }
}
