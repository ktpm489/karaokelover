package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.services.responses.zingmp3.ZingArtistDetail;

/**
 * Created by duclm on 7/28/2016.
 */

public class EventFinishLoadingArtistDetailInfoAndVideos {
    private ZingArtistDetail mZingArtistDetail;
    private List<VideoSearchItem> mListVideos;

    public EventFinishLoadingArtistDetailInfoAndVideos(ZingArtistDetail zingArtistDetail,
                                                       List<VideoSearchItem> listVideos) {
        this.mZingArtistDetail = zingArtistDetail;
        this.mListVideos = listVideos;
    }

    public ZingArtistDetail getArtistDetailInfo() {
        return mZingArtistDetail;
    }

    public List<VideoSearchItem> getArtistVideos() {
        return mListVideos;
    }
}
