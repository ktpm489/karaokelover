package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;

/**
 * Created by duclm on 7/28/2016.
 */

public class EventFinishLoadingHotTrendAndArtist {
    private List<ResponseSnippetStatistics> mListHotTrendKaraokes;
    private List<ArtistWithKaraoke> mListHotArtistWithKaraokes;

    public EventFinishLoadingHotTrendAndArtist(List<ResponseSnippetStatistics> listHotTrendKaraokes,
                                               List<ArtistWithKaraoke> listHotArtistWithKaraokes) {
        this.mListHotArtistWithKaraokes = listHotArtistWithKaraokes;
        this.mListHotTrendKaraokes = listHotTrendKaraokes;
    }

    public List<ResponseSnippetStatistics> getListHotTrendKaraokes() {
        return mListHotTrendKaraokes;
    }

    public List<ArtistWithKaraoke> getListHotArtistWithKaraokes() {
        return mListHotArtistWithKaraokes;
    }
}
