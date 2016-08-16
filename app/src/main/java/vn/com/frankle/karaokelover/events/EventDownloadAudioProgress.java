package vn.com.frankle.karaokelover.events;

import vn.com.frankle.karaokelover.models.YoutubeAudioDownload;

/**
 * Created by duclm on 8/16/2016.
 */

public class EventDownloadAudioProgress {
    private YoutubeAudioDownload currentDownload;

    public EventDownloadAudioProgress(YoutubeAudioDownload currentDownload) {
        this.currentDownload = currentDownload;
    }

    public int getProgress(){
        return this.currentDownload.getProgress();
    }

    public int getDownloadedSize(){
        return this.currentDownload.getCurrentFileSize();
    }

    public int getTotalFileSize(){
        return this.currentDownload.getTotalFileSize();
    }
}
