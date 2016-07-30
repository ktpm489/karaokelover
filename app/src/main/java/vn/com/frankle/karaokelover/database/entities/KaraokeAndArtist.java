package vn.com.frankle.karaokelover.database.entities;

/**
 * Created by duclm on 7/24/2016.
 */

public final class KaraokeAndArtist {

    private String artist;

    private String video_id;

    public KaraokeAndArtist(String artist, String video_id) {
        this.artist = artist;
        this.video_id = video_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }
}
