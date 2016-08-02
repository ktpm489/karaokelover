package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/**
 * Created by duclm on 8/2/2016.
 */
@Generated("org.jsonschema2pojo")
public class ResponseStatisticContentDetails {
    @SerializedName("items")
    @Expose
    private List<ItemStatisticsContentDetails> items = new ArrayList<ItemStatisticsContentDetails>();

    /**
     * Get duration of vidoe in format ISO 8601
     * @return duration or empty string
     */
    public String getDurationISO8601Format(){
        if (items.size() > 0){
            return items.get(0).getContentDetails().getDuration();
        }
        return new String();
    }

    /**
     * Get like count of this video
     * @return
     */
    public String getLikeCount(){
        if (items.size() > 0){
            return items.get(0).getStatistics().getLikeCount();
        }
        return new String("0");
    }

    /**
     * Get view count of this video
     * @return
     */
    public String getViewCount(){
        if (items.size() > 0){
            return items.get(0).getStatistics().getViewCount();
        }
        return new String("0");
    }

}
