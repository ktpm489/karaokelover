package vn.com.frankle.karaokelover.services.responses.youtube.playlist;

import com.google.gson.annotations.SerializedName;

import vn.com.frankle.karaokelover.models.Thumbnails;

public class Snippet {
    @SerializedName("publishedAt")
    private String publishedAt;
    @SerializedName("channelId")
    private String channelId;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("thumbnails")
    private Thumbnails thumbnails;
    @SerializedName("channelTitle")
    private String channelTitle;
    @SerializedName("playlistId")
    private String playlistId;
    @SerializedName("position")
    private int position;
    @SerializedName("resourceId")
    private ResourceId resourceId;

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }
}
