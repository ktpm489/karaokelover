package vn.com.frankle.karaokelover.database.entities;

import android.support.annotation.NonNull;

import java.util.ArrayList;
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

    @NonNull
    public String getArtist() {
        return artist;
    }

    @NonNull
    public List<String> getKaraokes() {
        return karaokes;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setKaraokes(List<String> karaokes) {
        this.karaokes = karaokes;
    }

    public static ArtistWithKaraoke getInstance(List<KaraokeAndArtist> list) {
        List<String> video_ids = new ArrayList<>();
        for (KaraokeAndArtist karaokeAndArtist : list) {
            video_ids.add(karaokeAndArtist.getVideo_id());
        }
        return new ArtistWithKaraoke(list.get(0).getArtist(), video_ids);
    }

    public List<ResponseSnippetContentDetails> getResponseYoutubeVideos() {
        return responseYoutubeVideos;
    }

    public void setResponseYoutubeVideos(List<ResponseSnippetContentDetails> responseYoutubeVideos) {
        this.responseYoutubeVideos = responseYoutubeVideos;
    }
}
