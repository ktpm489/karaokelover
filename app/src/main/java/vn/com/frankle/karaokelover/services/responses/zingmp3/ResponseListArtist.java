package vn.com.frankle.karaokelover.services.responses.zingmp3;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/**
 * Created by duclm on 9/19/2016.
 */
@Generated("org.jsonschema2pojo")
public class ResponseListArtist {
    @SerializedName("docs")
    @Expose
    private List<ZingArtist> zingArtists = new ArrayList<ZingArtist>();

    /**
     * @return The zingArtists
     */
    public List<ZingArtist> getZingArtists() {
        return zingArtists;
    }

    /**
     * @param zingArtists The zingArtists
     */
    public void setZingArtists(List<ZingArtist> zingArtists) {
        this.zingArtists = zingArtists;
    }
}
