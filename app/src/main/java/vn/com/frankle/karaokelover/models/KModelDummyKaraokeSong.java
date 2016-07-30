package vn.com.frankle.karaokelover.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duclm on 7/17/2016.
 */

public class KModelDummyKaraokeSong {
    private String title;
    private int duration;
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    // Tobe removed
    public KModelDummyKaraokeSong(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // Tobe removed
    public static List<KModelDummyKaraokeSong> createDummyHotKaraokeSongList(int artistID) {
        List<KModelDummyKaraokeSong> dummyList = new ArrayList<>();
        switch (artistID) {
            case 1:
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Chưa bao giờ"));
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Dấu mưa"));
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Chiều nay không có mưa bay"));
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Chưa bao giờ"));
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Trót yêu"));
                break;
            case 2:
                dummyList.add(new KModelDummyKaraokeSong("Không phải dạng vừa đâu"));
                dummyList.add(new KModelDummyKaraokeSong("Em của ngày hôm qua"));
                dummyList.add(new KModelDummyKaraokeSong("[Karaoke] Chắc ai đó sẽ về"));
                dummyList.add(new KModelDummyKaraokeSong("Buông đôi tay nhau ra"));
                dummyList.add(new KModelDummyKaraokeSong("Nắng ấm xa dần"));
                break;
            case 3:
                dummyList.add(new KModelDummyKaraokeSong("Shape Of My Heart"));
                dummyList.add(new KModelDummyKaraokeSong("Drowning"));
                dummyList.add(new KModelDummyKaraokeSong("Inconsolable"));
                dummyList.add(new KModelDummyKaraokeSong("Show me the meaning of being lonely [Karaoke][Full beat]"));
                dummyList.add(new KModelDummyKaraokeSong("More than that"));
                break;
            default:
                break;

        }

        return dummyList;
    }
}
