package vn.com.frankle.karaokelover.services;

import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ResponseListArtist;

/**
 * Created by duclm on 9/19/2016.
 */

public interface ZingMp3APIEndpointInterface {
    @GET
    Observable<ResponseListArtist> getArtist(@Url String url);
}
