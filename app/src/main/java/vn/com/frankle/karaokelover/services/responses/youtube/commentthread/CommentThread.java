package vn.com.frankle.karaokelover.services.responses.youtube.commentthread;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 10/11/2016.
 */

@Generated("org.jsonschema2pojo")
public class CommentThread implements Comparable<CommentThread> {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("snippet")
    @Expose
    private Thread thread;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public int compareTo(CommentThread commentThread) {
        if (this.id.equals(commentThread.getId())) {
            return 0;
        }
        return -1;
    }
}
