package vn.com.frankle.karaokelover.services.responses.youtube.commentthread;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by duclm on 10/11/2016.
 */

public class TopLevelComment {
    @SerializedName("snippet")
    @Expose
    private Comment comment;

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
