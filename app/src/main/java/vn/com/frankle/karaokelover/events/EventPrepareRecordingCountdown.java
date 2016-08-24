package vn.com.frankle.karaokelover.events;

/**
 * Created by duclm on 8/24/2016.
 */

public class EventPrepareRecordingCountdown {
    private int current;

    public EventPrepareRecordingCountdown(int current){
        this.current = current;
    }

    public int getCurrentValue(){
        return this.current;
    }
}
