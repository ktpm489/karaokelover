package vn.com.frankle.karaokelover.events;

import vn.com.frankle.karaokelover.database.entities.VideoSearchItem;

/**
 * Created by duclm on 14-Nov-16.
 */
public class EventPopupMenuItemClick {

    public interface ACTION {
        int ADD_FAVORITE = 1;
        int REMOVE_FAVORITE = 2;
        int ADD_PLAYLIST = 3;
    }

    private VideoSearchItem data;
    private int action;

    public EventPopupMenuItemClick(VideoSearchItem data, int action) {
        this.data = data;
        this.action = action;
    }

    public VideoSearchItem getData() {
        return data;
    }

    public int getAction() {
        return action;
    }
}
