package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import vn.com.frankle.karaokelover.services.responses.youtube.commentthread.CommentThread;

/**
 * Created by duclm on 10/11/2016.
 */

@Generated("org.jsonschema2pojo")
public class ResponseCommentThreads {
    @SerializedName("nextPageToken")
    @Expose
    private String nextPageToken;

    @SerializedName("items")
    @Expose
    private List<CommentThread> commentThreads = new ArrayList<CommentThread>();

    /**
     * @return The nextPageToken
     */
    public String getNextPageToken() {
        return nextPageToken;
    }

    /**
     * @param nextPageToken The nextPageToken
     */
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    /**
     * @return The items
     */
    public List<CommentThread> getCommentThreads() {
        return commentThreads;
    }

    /**
     * @param items The items
     */
    public void setCommentThreads(List<CommentThread> commentThreads) {
        this.commentThreads = commentThreads;
    }
}
