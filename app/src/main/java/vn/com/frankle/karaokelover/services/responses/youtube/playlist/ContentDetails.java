package vn.com.frankle.karaokelover.services.responses.youtube.playlist;

import com.google.gson.annotations.SerializedName;

public class ContentDetails {
    @SerializedName("itemCount")
    private int itemCount;

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
