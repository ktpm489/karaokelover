package vn.com.frankle.karaokelover.events;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;

/**
 * Created by duclm on 7/24/2016.
 */

public class EventLoadedLocalHotArtistWithKaraoke {
    private ArtistWithKaraoke artistWithKaraoke;

    public EventLoadedLocalHotArtistWithKaraoke(ArtistWithKaraoke artistWithKaraoke) {
        this.artistWithKaraoke = artistWithKaraoke;
    }

    public ArtistWithKaraoke getArtistWithKaraoke() {
        return artistWithKaraoke;
    }
}
