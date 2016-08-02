package vn.com.frankle.karaokelover.services;

import android.util.Log;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.database.entities.ArtistWithKaraoke;
import vn.com.frankle.karaokelover.database.entities.DAOArtist;
import vn.com.frankle.karaokelover.database.entities.DAOHotTrend;
import vn.com.frankle.karaokelover.database.entities.KaraokeAndArtist;
import vn.com.frankle.karaokelover.database.tables.ArtistTable;
import vn.com.frankle.karaokelover.database.tables.KaraokeTable;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSearch;
import vn.com.frankle.karaokelover.models.ResponseYoutubeSnippetStatistics;

/**
 * Created by duclm on 7/28/2016.
 */

public class ReactiveHelper {

    /**
     * Get observable to get list of ResponseYoutubeSnippetStatistics from list of DAOHotTrend
     *
     * @param listDAOHotTrend : list hot trend to be acquired
     * @return
     */
    public static Observable<List<ResponseYoutubeSnippetStatistics>> getObsListHotTrend(List<DAOHotTrend> listDAOHotTrend) {
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

    /**
     * Search for karaoke videos
     *
     * @param query : video title to search
     * @return
     */
    public static Observable<ResponseYoutubeSearch> searchKarokeVideos(String query) {
        // Append "karaoke" at the end of query string for searching for Karaoke video
        String karaokeQuery = new StringBuilder(query).append(" karaoke").toString();
        return KApplication.getRxYoutubeAPIService().searchKaraokeVideos(karaokeQuery);
    }
}
