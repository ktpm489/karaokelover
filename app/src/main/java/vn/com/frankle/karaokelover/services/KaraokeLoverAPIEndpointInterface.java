package vn.com.frankle.karaokelover.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;
import vn.com.frankle.karaokelover.models.KModelDummyKaraokeSong;

/**
 * Created by duclm on 7/21/2016.
 */

public interface KaraokeLoverAPIEndpointInterface {

    @GET("hotkaraokes")
    Call<List<KModelDummyKaraokeSong>> getHotKaraoke();

    @GET("hotkaraokes")
    Observable<List<KModelDummyKaraokeSong>> getHotKaraokes();

    @GET("hotkaraokes")
    Observable<KModelDummyKaraokeSong> getUser();
}
