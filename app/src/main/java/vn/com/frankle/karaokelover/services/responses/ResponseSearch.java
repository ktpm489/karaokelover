
package vn.com.frankle.karaokelover.services.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ResponseSearch {
    @SerializedName("items")
    @Expose
    private List<ItemSearch> items = new ArrayList<ItemSearch>();

    public ResponseSearch(){
        this.items = new ArrayList<>();
    }

    /**
     * @return The items
     */
    public List<ItemSearch> getItems() {
        return items;
    }

    /**
     * @param items The items
     */
    public void setItems(List<ItemSearch> items) {
        this.items = items;
    }
}
