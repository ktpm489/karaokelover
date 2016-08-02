package vn.com.frankle.karaokelover.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 8/1/2016.
 */

@Generated("org.jsonschema2pojo")
public class ItemSnippetSearch {

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
