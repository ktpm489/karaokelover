package vn.com.frankle.karaokelover.events;

/**
 * Created by duclm on 11/1/2016.
 */

public class EventLoadHotTrendAndArtistsError {

    private Throwable mError;

    public EventLoadHotTrendAndArtistsError(Throwable e) {
        this.mError = e;
    }

    public Throwable getException() {
        return this.mError;
    }
}
