package vn.com.frankle.karaokelover.services;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.frankle.karaokelover.services.responses.ResponseSearch;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetContentDetails;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;
import vn.com.frankle.karaokelover.services.responses.ResponseStatisticContentDetails;

/**
 * Created by duclm on 7/23/2016.
 */

public interface YoutubeAPIEndpointInterface {
    @GET("videos?part=snippet,statistics&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseSnippetStatistics> getYoutubeVideoById(@Query("id") String videoID);

    @GET("videos?part=snippet,contentDetails&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseSnippetContentDetails> getYoutubeDetailContentById(@Query("id") String videoID);

    @GET("search?part=snippet&maxResults=15&type=video&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseSearch> searchKaraokeVideos(@Query("q") String searchQuery);

    @GET("videos?part=statistics,contentDetails&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseStatisticContentDetails> getStatisticContentDetailById(@Query("id") String videoID);
}
