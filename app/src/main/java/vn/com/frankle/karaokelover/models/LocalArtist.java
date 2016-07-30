package vn.com.frankle.karaokelover.models;

/**
 * Created by duclm on 7/24/2016.
 */

public class LocalArtist {
    private int id;
    private String name;
    private int vietnam;

    public LocalArtist(int id, String name, int vietnam) {
        this.id = id;
        this.name = name;
        this.vietnam = vietnam;
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

    public int getVietnam() {
        return vietnam;
    }

    public void setVietnam(int vietnam) {
        this.vietnam = vietnam;
    }
}
