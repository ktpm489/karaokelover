
package vn.com.frankle.karaokelover.models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Thumbnails {

    @SerializedName("default")
    @Expose
    private Default _default;
    @SerializedName("medium")
    @Expose
    private Medium medium;
    @SerializedName("high")
    @Expose
    private High high;
    @SerializedName("standard")
    @Expose
    private Standard standard;
    @SerializedName("maxres")
    @Expose
    private Maxres maxres;

    /**
     * 
     * @return
     *     The _default
     */
    public Default getDefault() {
        return _default;
    }

    /**
     * 
     * @param _default
     *     The default
     */
    public void setDefault(Default _default) {
        this._default = _default;
    }

    /**
     * 
     * @return
     *     The medium
     */
    public Medium getMedium() {
        return medium;
    }

    /**
     * 
     * @param medium
     *     The medium
     */
    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    /**
     * 
     * @return
     *     The high
     */
    public High getHigh() {
        return high;
    }

    /**
     * 
     * @param high
     *     The high
     */
    public void setHigh(High high) {
        this.high = high;
    }

    /**
     * 
     * @return
     *     The standard
     */
    public Standard getStandard() {
        return standard;
    }

    /**
     * 
     * @param standard
     *     The standard
     */
    public void setStandard(Standard standard) {
        this.standard = standard;
    }

    /**
     * 
     * @return
     *     The maxres
     */
    public Maxres getMaxres() {
        return maxres;
    }

    /**
     * 
     * @param maxres
     *     The maxres
     */
    public void setMaxres(Maxres maxres) {
        this.maxres = maxres;
    }

}
