package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

import vn.com.frankle.karaokelover.models.ContentDetails;
import vn.com.frankle.karaokelover.models.Statistics;

/**
 * Created by duclm on 8/2/2016.
 */
@Generated("org.jsonschema2pojo")
public class ItemStatisticsContentDetails {
    @SerializedName("contentDetails")
    @Expose
    private ContentDetails contentDetails;
    @SerializedName("statistics")
    @Expose
    private Statistics statistics;

    /**
     * @return The contentDetails
     */
    public ContentDetails getContentDetails() {
        return contentDetails;
    }

    /**
     * @param contentDetails The contentDetails
     */
    public void setContentDetails(ContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }

    /**
     * @return The statistics
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * @param statistics The statistics
     */
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
