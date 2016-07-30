package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSnippetStatistics;

/**
 * Created by duclm on 7/28/2016.
 */

public class EventFinishLoadingHotTrendAndArtist {
    private List<ResponseYoutubeSnippetStatistics> mListHotTrendKaraokes;
    private List<ArtistWithKaraoke> mListHotArtistWithKaraokes;

    public EventFinishLoadingHotTrendAndArtist(List<ResponseYoutubeSnippetStatistics> listHotTrendKaraokes,
                                               List<ArtistWithKaraoke> listHotArtistWithKaraokes) {
        this.mListHotArtistWithKaraokes = listHotArtistWithKaraokes;
        this.mListHotTrendKaraokes = listHotTrendKaraokes;
    }

    public List<ResponseYoutubeSnippetStatistics> getListHotTrendKaraokes() {
        return mListHotTrendKaraokes;
    }

    public List<ArtistWithKaraoke> getListHotArtistWithKaraokes() {
        return mListHotArtistWithKaraokes;
    }
}
