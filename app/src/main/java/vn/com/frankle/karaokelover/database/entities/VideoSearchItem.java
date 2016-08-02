package vn.com.frankle.karaokelover.database.entities;

import vn.com.frankle.karaokelover.Utils;
import vn.com.frankle.karaokelover.models.Thumbnails;

/**
 * Created by duclm on 8/2/2016.
 */

public class VideoSearchItem {
    private String mTitle;
    private String mDuration;
    private String mViewCount;
    private String mLikeCount;
    private Thumbnails mThumbnails;

    public VideoSearchItem(String title, String duration, String viewcount, String likecount, Thumbnails thumbnail) {
        this.mTitle = title;
        this.mDuration = duration;
        this.mViewCount = viewcount;
        this.mLikeCount = likecount;
        this.mThumbnails = thumbnail;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDuration() {
        return Utils.convertYoutubeTimeformat(mDuration);
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getViewCount() {
        return Utils.getViewCount(this.mViewCount);
    }

    public void setViewCount(String mViewCount) {
        this.mViewCount = mViewCount;
    }

    public String getLikeCount() {
        return Utils.getLikeCount(this.mLikeCount);
    }

    public void setLikeCount(String mLikeCount) {
        this.mLikeCount = mLikeCount;
    }

    public String getThumbnails() {
        return Utils.getThumbnailURL(this.mThumbnails);
    }

    public void setThumbnails(Thumbnails mThumbnails) {
        this.mThumbnails = mThumbnails;
    }
}
