package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;

/**
 * Created by duclm on 7/26/2016.
 */

public class EventFinishLoadingHotArtistWithKaraoke {
    private List<ArtistWithKaraoke> mListArtistWithYoutubeVideos;
    public EventFinishLoadingHotArtistWithKaraoke(List<ArtistWithKaraoke> listArtistWithYoutubeVideos){
        this.mListArtistWithYoutubeVideos = listArtistWithYoutubeVideos;
    }

    public List<ArtistWithKaraoke> getListHotArtistWithYoutubeVideos(){
        return this.mListArtistWithYoutubeVideos;
    }
}
