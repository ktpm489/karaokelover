package vn.com.frankle.karaokelover.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ContentDetails {

    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("dimension")
    @Expose
    private String dimension;
    @SerializedName("definition")
    @Expose
    private String definition;
    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("licensedContent")
    @Expose
    private boolean licensedContent;
    @SerializedName("projection")
    @Expose
    private String projection;

    /**
     * @return The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return The dimension
     */
    public String getDimension() {
        return dimension;
    }

    /**
     * @param dimension The dimension
     */
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    /**
     * @return The definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition The definition
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * @return The caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption The caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return The licensedContent
     */
    public boolean isLicensedContent() {
        return licensedContent;
    }

    /**
     * @param licensedContent The licensedContent
     */
    public void setLicensedContent(boolean licensedContent) {
        this.licensedContent = licensedContent;
    }

    /**
     * @return The projection
     */
    public String getProjection() {
        return projection;
    }

    /**
     * @param projection The projection
     */
    public void setProjection(String projection) {
        this.projection = projection;
    }

}