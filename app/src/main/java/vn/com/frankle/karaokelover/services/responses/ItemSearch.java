package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

import vn.com.frankle.karaokelover.models.Id;
import vn.com.frankle.karaokelover.models.Snippet;

/**
 * Created by duclm on 8/1/2016.
 */

@Generated("org.jsonschema2pojo")
public class ItemSearch {

    @SerializedName("id")
    @Expose
    private Id id;

    @SerializedName("snippet")
    @Expose
    private Snippet snippet;

    /**
     * @return The id
     */
    public Id getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Id id) {
        this.id = id;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }
}
