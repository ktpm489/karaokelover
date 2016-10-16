package vn.com.frankle.karaokelover.services.responses.youtube.commentthread;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 10/11/2016.
 */
@Generated("org.jsonschema2pojo")
public class Comment {
    @SerializedName("authorDisplayName")
    @Expose
    private String authorDisplayName;
    @SerializedName("authorProfileImageUrl")
    @Expose
    private String authorProfileImageUrl;
    @SerializedName("authorChannelUrl")
    @Expose
    private String authorChannelUrl;
    @SerializedName("videoId")
    @Expose
    private String videoId;
    @SerializedName("textDisplay")
    @Expose
    private String textDisplay;
    @SerializedName("canRate")
    @Expose
    private boolean canRate;
    @SerializedName("viewerRating")
    @Expose
    private String viewerRating;
    @SerializedName("likeCount")
    @Expose
    private int likeCount;
    @SerializedName("publishedAt")
    @Expose
    private String publishedAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;

    /**
     * @return The authorDisplayName
     */
    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    /**
     * @param authorDisplayName The authorDisplayName
     */
    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    /**
     * @return The authorProfileImageUrl
     */
    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl.replace("sz=50", "sz=100");
    }

    /**
     * @param authorProfileImageUrl The authorProfileImageUrl
     */
    public void setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
    }

    /**
     * @return The authorChannelUrl
     */
    public String getAuthorChannelUrl() {
        return authorChannelUrl;
    }

    /**
     * @param authorChannelUrl The authorChannelUrl
     */
    public void setAuthorChannelUrl(String authorChannelUrl) {
        this.authorChannelUrl = authorChannelUrl;
    }

    /**
     * @return The videoId
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * @param videoId The videoId
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * @return The textDisplay
     */
    public String getTextDisplay() {
        return textDisplay;
    }

    /**
     * @param textDisplay The textDisplay
     */
    public void setTextDisplay(String textDisplay) {
        this.textDisplay = textDisplay;
    }

    /**
     * @return The canRate
     */
    public boolean isCanRate() {
        return canRate;
    }

    /**
     * @param canRate The canRate
     */
    public void setCanRate(boolean canRate) {
        this.canRate = canRate;
    }

    /**
     * @return The viewerRating
     */
    public String getViewerRating() {
        return viewerRating;
    }

    /**
     * @param viewerRating The viewerRating
     */
    public void setViewerRating(String viewerRating) {
        this.viewerRating = viewerRating;
    }

    /**
     * @return The likeCount
     */
    public int getLikeCount() {
        return likeCount;
    }

    /**
     * @param likeCount The likeCount
     */
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * @return The publishedAt
     */
    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * @param publishedAt The publishedAt
     */
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * @return The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt The updatedAt
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
