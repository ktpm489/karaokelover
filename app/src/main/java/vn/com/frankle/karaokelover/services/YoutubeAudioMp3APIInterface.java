package vn.com.frankle.karaokelover.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import rx.Observable;
import vn.com.frankle.karaokelover.services.responses.ResponseAudioMp3;

/**
 * Created by duclm on 8/15/2016.
 */

public interface YoutubeAudioMp3APIInterface {
    @GET("/download/get/?")
    @Streaming
    Call<ResponseBody> downloadAudioFile(@Query("i") String uri);
}
