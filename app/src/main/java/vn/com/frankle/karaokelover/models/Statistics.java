package vn.com.frankle.karaokelover.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 7/23/2016.
 */
@Generated("org.jsonschema2pojo")
public class Statistics {
    @SerializedName("viewCount")
    @Expose
    private String viewCount;
    @SerializedName("likeCount")
    @Expose
    private String likeCount;
    @SerializedName("dislikeCount")
    @Expose
    private String dislikeCount;
    @SerializedName("favoriteCount")
    @Expose
    private String favoriteCount;
    @SerializedName("commentCount")
    @Expose
    private String commentCount;

    /**
     * @return The viewCount
     */
    public String getViewCount() {
        return viewCount;
    }

    /**
     * @param viewCount The viewCount
     */
    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * @return The likeCount
     */
    public String getLikeCount() {
        return likeCount;
    }

    /**
     * @param likeCount The likeCount
     */
    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * @return The dislikeCount
     */
    public String getDislikeCount() {
        return dislikeCount;
    }

    /**
     * @param dislikeCount The dislikeCount
     */
    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    /**
     * @return The favoriteCount
     */
    public String getFavoriteCount() {
        return favoriteCount;
    }

    /**
     * @param favoriteCount The favoriteCount
     */
    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    /**
     * @return The commentCount
     */
    public String getCommentCount() {
        return commentCount;
    }

    /**
     * @param commentCount The commentCount
     */
    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }
}
