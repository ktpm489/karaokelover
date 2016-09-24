package vn.com.frankle.karaokelover.services;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ResponseListArtist;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;

/**
 * Created by duclm on 9/19/2016.
 */

public interface ZingMp3APIEndpointInterface {
    @GET
    Observable<ResponseListArtist> getArtist(@Url String url);

    @GET("mobile/artist/getartistinfo?keycode=b319bd16be6d049fdb66c0752298ca30")
    Observable<ZingArtistDetail> getArtistDetail(@Query("requestdata") String jsonArtistId);
}
