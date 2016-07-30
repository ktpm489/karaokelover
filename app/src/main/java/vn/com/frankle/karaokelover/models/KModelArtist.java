package vn.com.frankle.karaokelover.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duclm on 7/19/2016.
 */

public class KModelArtist {
    private int id;
    private String name;

    public KModelArtist(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // To-be removed
    public List<KModelDummyKaraokeSong> getHotSongs(){
        return KModelDummyKaraokeSong.createDummyHotKaraokeSongList(id);
    }

    // To-be removed
    public static List<KModelArtist> getHotArtists(){
        List<KModelArtist> dummyList = new ArrayList<>();
        dummyList.add(new KModelArtist(1, "Trung Quân Idol"));
        dummyList.add(new KModelArtist(2, "Sơn Tùng - MTP"));
        dummyList.add(new KModelArtist(3, "Backstreet Boys"));
        return dummyList;
    }
}
