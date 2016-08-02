package vn.com.frankle.karaokelover.services;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSearch;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSnippetContentDetails;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSnippetStatistics;

/**
 * Created by duclm on 7/23/2016.
 */

public interface YoutubeAPIEndpointInterface {
    @GET("videos?part=snippet,statistics&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseYoutubeSnippetStatistics> getYoutubeVideoById(@Query("id") String videoID);

    @GET("videos?part=snippet,contentDetails&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseYoutubeSnippetContentDetails> getYoutubeDetailContentById(@Query("id") String videoID);

    @GET("search?part=snippet&maxResults=20&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    Observable<ResponseYoutubeSearch> searchKaraokeVideos(@Query("q") String searchQuery);
}
