package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.models.ResponseYoutubeSnippetStatistics;

/**
 * Created by duclm on 7/23/2016.
 */

public class EventLoadedHotKaraoke {
    private List<ResponseYoutubeSnippetStatistics> listHotTrendKaraokes;

    public EventLoadedHotKaraoke(List<ResponseYoutubeSnippetStatistics> data) {
        this.listHotTrendKaraokes = data;
    }

    public List<ResponseYoutubeSnippetStatistics> getHotTrendKaraokes() {
        return this.listHotTrendKaraokes;
    }
}
