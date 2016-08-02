package vn.com.frankle.karaokelover.events;

import java.util.List;

import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;

/**
 * Created by duclm on 7/23/2016.
 */

public class EventLoadedHotKaraoke {
    private List<ResponseSnippetStatistics> listHotTrendKaraokes;

    public EventLoadedHotKaraoke(List<ResponseSnippetStatistics> data) {
        this.listHotTrendKaraokes = data;
    }

    public List<ResponseSnippetStatistics> getHotTrendKaraokes() {
        return this.listHotTrendKaraokes;
    }
}
