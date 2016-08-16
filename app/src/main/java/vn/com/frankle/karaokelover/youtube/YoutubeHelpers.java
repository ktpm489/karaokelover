package vn.com.frankle.karaokelover.youtube;

import android.support.annotation.NonNull;

/**
 * Created by duclm on 8/15/2016.
 */

public class YoutubeHelpers {

    public static String buildFullYoutubeVideoURLWithId(@NonNull String videoId) {
        final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
        if (videoId.isEmpty()) {
            throw new NullPointerException("Empty video id");
        }
        return YOUTUBE_BASE_URL + videoId;
    }
}
