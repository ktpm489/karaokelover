package vn.com.frankle.karaokelover.database.entities;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.com.frankle.karaokelover.services.responses.ResponseSnippetContentDetails;

import static java.util.Collections.unmodifiableList;

/**
 * Created by duclm on 7/24/2016.
 */

public final class ArtistWithKaraoke {

    private String artist;

    private List<String> karaokes;

    private List<ResponseSnippetContentDetails> responseYoutubeVideos;

    private List<VideoSearchItem> mVideosList;

    public ArtistWithKaraoke() {
        artist = null;
        karaokes = null;
    }

    public ArtistWithKaraoke(String artist, List<String> karaokes, List<ResponseSnippetContentDetails> listYoutubeVideos) {
        this.artist = artist;
        this.karaokes = karaokes;
        this.responseYoutubeVideos = listYoutubeVideos;
    }

    public ArtistWithKaraoke(@NonNull String artist, @NonNull List<String> karaokes) {
        this.artist = artist;
        this.karaokes = unmodifiableList(karaokes); // We prefer immutable entities
    }

    public ArtistWithKaraoke(String artist, List<String> karaokes, List<ResponseSnippetContentDetails> listYoutubeVideos, List<VideoSearchItem> listVideosSearch) {
        this.artist = artist;
        this.karaokes = karaokes;
        this.responseYoutubeVideos = listYoutubeVideos;
        this.mVideosList = listVideosSearch;
    }

    public static ArtistWithKaraoke getInstance(List<KaraokeAndArtist> list) {
        List<String> video_ids = new ArrayList<>();
        for (KaraokeAndArtist karaokeAndArtist : list) {
            video_ids.add(karaokeAndArtist.getVideo_id());
        }
        return new ArtistWithKaraoke(list.get(0).getArtist(), video_ids);
    }

    public String debugGetVideoTitleList() {
        ArrayList<String> titleList = new ArrayList<>();
        for (int i = 0; i < mVideosList.size(); i++) {
            titleList.add(mVideosList.get(i).getTitle());
        }

        return Arrays.toString(titleList.toArray());
    }

    @NonNull
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @NonNull
    public List<String> getKaraokes() {
        return karaokes;
    }

    public void setKaraokes(List<String> karaokes) {
        this.karaokes = karaokes;
    }

    public List<ResponseSnippetContentDetails> getResponseYoutubeVideos() {
        return responseYoutubeVideos;
    }

    public void setResponseYoutubeVideos(List<ResponseSnippetContentDetails> responseYoutubeVideos) {
        this.responseYoutubeVideos = responseYoutubeVideos;
    }

    public List<VideoSearchItem> getVideoList() {
        return this.mVideosList;
    }
}
