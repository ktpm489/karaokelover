package vn.com.frankle.karaokelover.services.responses.youtube.playlist;

import com.google.gson.annotations.SerializedName;

public class Items {
    @SerializedName("kind")
    private String kind;
    @SerializedName("etag")
    private String etag;
    @SerializedName("id")
    private String id;
    @SerializedName("snippet")
    private Snippet snippet;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }
}
