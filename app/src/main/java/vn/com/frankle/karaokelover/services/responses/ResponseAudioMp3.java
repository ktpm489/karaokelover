package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ResponseAudioMp3 {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("length")
    @Expose
    private String length;
    @SerializedName("link")
    @Expose
    private String link;

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The length
     */
    public String getLength() {
        return length;
    }

    /**
     * @param length The length
     */
    public void setLength(String length) {
        this.length = length;
    }

    /**
     * @return The link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(String link) {
        this.link = link;
    }

}