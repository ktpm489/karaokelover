package vn.com.frankle.karaokelover.services

import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable
import vn.com.frankle.karaokelover.services.responses.ResponseCommentThreads
import vn.com.frankle.karaokelover.services.responses.ResponseSearch
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetContentDetails
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics
import vn.com.frankle.karaokelover.services.responses.ResponseStatisticContentDetails

/**
 * Created by duclm on 7/23/2016.
 */

interface YoutubeAPIEndpointInterface {
    @GET("videos?part=snippet,statistics&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun getYoutubeVideoById(@Query("id") videoID: String): Observable<ResponseSnippetStatistics>

    @GET("videos?part=snippet,contentDetails&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun getYoutubeDetailContentById(@Query("id") videoID: String): Observable<ResponseSnippetContentDetails>

    @GET("search?part=snippet&maxResults=15&type=video&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun searchKaraokeVideos(@Query("q") searchQuery: String): Observable<ResponseSearch>

    @GET("search?part=snippet&type=video&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun searchKaraokeVideos(@Query("q") searchQuery: String, @Query("maxResults") maxResult: Int): Observable<ResponseSearch>

    @GET("search?part=snippet&maxResults=15&type=video&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun searchYoutubeVideoNext(@Query("q") searchQuery: String, @Query("pageToken") nextPageToken: String): Observable<ResponseSearch>

    @GET("videos?part=statistics,contentDetails&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun getStatisticContentDetailById(@Query("id") videoID: String): Observable<ResponseStatisticContentDetails>

    @GET("commentThreads?part=snippet&maxResults=20&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun getVideoComments(@Query("videoId") videoId: String): Observable<ResponseCommentThreads>

    @GET("commentThreads?part=snippet&maxResults=20&order=relevance&key=AIzaSyC-DSJP7roLCjod8aOnzAq0o2-L0NJZXYU")
    fun getVideoCommentsNext(@Query("videoId") videoId: String, @Query("pageToken") nextPageToken: String): Observable<ResponseCommentThreads>
}
