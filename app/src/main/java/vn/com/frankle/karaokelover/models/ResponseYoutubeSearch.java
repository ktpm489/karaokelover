
package vn.com.frankle.karaokelover.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ResponseYoutubeSearch {
    @SerializedName("items")
    @Expose
    private List<ItemSnippetSearch> items = new ArrayList<ItemSnippetSearch>();

    public ResponseYoutubeSearch(){
        this.items = new ArrayList<>();
    }

    /**
     * @return The items
     */
    public List<ItemSnippetSearch> getItems() {
        return items;
    }

    /**
     * @param items The items
     */
    public void setItems(List<ItemSnippetSearch> items) {
        this.items = items;
    }

}
