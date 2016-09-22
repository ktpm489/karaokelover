package vn.com.frankle.karaokelover.services;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrend;
import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.KaraokeTable;
import vn.com.frankle.karaokelover.models.Snippet;
import vn.com.frankle.karaokelover.services.responses.ItemSearch;
import vn.com.frankle.karaokelover.services.responses.ResponseSnippetStatistics;

/**
 * Created by duclm on 7/28/2016.
 */

public class ReactiveHelper {

    /**
     * Get observable to get list of ResponseSnippetStatistics from list of DAOHotTrend
     *
     * @param listDAOHotTrend : list hot trend to be acquired
     * @return
     */
    public static Observable<List<ResponseSnippetStatistics>> getObsListHotTrend(List<DAOHotTrend> listDAOHotTrend) {
        return Observable.from(listDAOHotTrend)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(daoHotTrend -> KApplication.getRxYoutubeAPIService().getYoutubeVideoById(daoHotTrend.getVideoId()))
                .toList();
    }

    public static Observable<List<ArtistWithKaraoke>> getListHotArtistWithKarokes(StorIOSQLite sqliteHandler, List<DAOArtist> listDAOArtist) {
        return Observable.from(listDAOArtist)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(artist -> getDBArtistWithKarokes(sqliteHandler, artist))
                .toList();
    }

    private static Observable<ArtistWithKaraoke> getDBArtistWithKarokes(StorIOSQLite sqliteHandler, DAOArtist artist) {
        return sqliteHandler
                .get()
                .listOfObjects(KaraokeAndArtist.class)
                .withQuery(RawQuery.builder()
                        .query("SELECT "
                                + KaraokeTable.COLUMN_VIDEOID + ", "
                                + ArtistTable.COLUMN_NAME
                                + " FROM " + KaraokeTable.TABLE
                                + " JOIN " + ArtistTable.TABLE
                                + " ON " + KaraokeTable.TABLE + "." + KaraokeTable.COLUMN_ARTIST
                                + " = " + ArtistTable.TABLE + "." + ArtistTable.COLUMN_ID
                                + " WHERE " + KaraokeTable.COLUMN_ARTIST
                                + " = ?")
                        .args(artist.getId())
                        .build())
                .prepare()
                .asRxObservable()
                .map(ArtistWithKaraoke::getInstance);
    }


    public static Observable<List<ArtistWithKaraoke>> getListHotKaraokeOfArtist(List<ArtistWithKaraoke> artistWithKaraokes) {
        return Observable.from(artistWithKaraokes)
                .concatMap(artistWithKaraoke -> Observable.from(artistWithKaraoke.getKaraokes())
                        .concatMap(s -> KApplication.getRxYoutubeAPIService().getYoutubeDetailContentById(s))
                        .toList()
                        .map(responseYoutubeSnippetContentDetailses -> new ArtistWithKaraoke(artistWithKaraoke.getArtist(), artistWithKaraoke.getKaraokes(), responseYoutubeSnippetContentDetailses)))
                .toList();
    }


    public static Observable<VideoSearchItem> getStatisticsContentDetails(ItemSearch itemSearch) {
        return KApplication.getRxYoutubeAPIService().getStatisticContentDetailById(itemSearch.getId().getVideoId())
                .map(responseStatisticContentDetails
                        -> new VideoSearchItem(itemSearch.getId().getVideoId(), itemSearch.getSnippet().getTitle(),
                        responseStatisticContentDetails.getDurationISO8601Format(),
                        responseStatisticContentDetails.getViewCount(),
                        responseStatisticContentDetails.getLikeCount(),
                        itemSearch.getSnippet().getThumbnails()));
    }

    public static Observable<VideoSearchItem> getStatisticsContentDetails(ResponseSnippetStatistics response) {
        String id = response.getItems().get(0).getId();
        Snippet snippet = response.getItems().get(0).getSnippet();
        return KApplication.getRxYoutubeAPIService().getStatisticContentDetailById(id)
                .map(responseStatisticContentDetails
                                -> new VideoSearchItem(id, snippet.getTitle(),
                                responseStatisticContentDetails.getDurationISO8601Format(),
                                responseStatisticContentDetails.getViewCount(),
                                responseStatisticContentDetails.getLikeCount(),
                                snippet.getThumbnails()
                        )
                );
    }

    /**
     * Search for karaoke videos
     *
     * @param query : video title to search
     * @return
     */
    public static Observable<List<VideoSearchItem>> searchKarokeVideos(String query) {
        // Append "karaoke" at the end of query string for searching for Karaoke video
        String karaokeQuery = query + " karaoke";
        return KApplication.getRxYoutubeAPIService()
                .searchKaraokeVideos(karaokeQuery)
                .concatMap(
                        responseSearch -> Observable.from(responseSearch.getItems())
                                .subscribeOn(Schedulers.newThread())
                                .concatMap(ReactiveHelper::getStatisticsContentDetails))
                .toList();
    }

    public static Observable<List<VideoSearchItem>> getFavoritesVideos(ArrayList<String> listFavoriteId) {
        return Observable.from(listFavoriteId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(s -> KApplication.getRxYoutubeAPIService().getYoutubeVideoById(s)
                        .concatMap(ReactiveHelper::getStatisticsContentDetails)
                )
                .toList();
    }
}
