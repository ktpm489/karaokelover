package vn.com.frankle.karaokelover.services.responses.youtube.commentthread;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 10/11/2016.
 */

@Generated("org.jsonschema2pojo")
public class Thread {
    @SerializedName("videoId")
    @Expose
    private String videoId;
    @SerializedName("topLevelComment")
    @Expose
    private TopLevelComment topLevelComment;
    @SerializedName("canReply")
    @Expose
    private boolean canReply;
    @SerializedName("totalReplyCount")
    @Expose
    private int totalReplyCount;
    @SerializedName("isPublic")
    @Expose
    private boolean isPublic;

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
     * @return The topLevelComment
     */
    public TopLevelComment getTopLevelComment() {
        return topLevelComment;
    }

    /**
     * @param topLevelComment The topLevelComment
     */
    public void setTopLevelComment(TopLevelComment topLevelComment) {
        this.topLevelComment = topLevelComment;
    }

    /**
     * @return The canReply
     */
    public boolean isCanReply() {
        return canReply;
    }

    /**
     * @param canReply The canReply
     */
    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
    }

    /**
     * @return The totalReplyCount
     */
    public int getTotalReplyCount() {
        return totalReplyCount;
    }

    /**
     * @param totalReplyCount The totalReplyCount
     */
    public void setTotalReplyCount(int totalReplyCount) {
        this.totalReplyCount = totalReplyCount;
    }

    /**
     * @return The isPublic
     */
    public boolean isIsPublic() {
        return isPublic;
    }

    /**
     * @param isPublic The isPublic
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
