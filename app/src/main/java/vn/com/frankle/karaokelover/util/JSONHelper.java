package vn.com.frankle.karaokelover.util;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by duclm on 9/19/2016.
 */

public class JSONHelper {

    public static JSONObject writeJSONDataDetail(String typeContent, String id) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("t", typeContent);
            jsonData.put("id", id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    public static JSONObject writeJSONDataArtistInfo(String id) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    /**
     * JSON data to get list of song chart (Vietnam, US-UK, Korean)
     */
    public static JSONObject writeJsonDataArtists(int type, int pageNumber) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", type);
            jsonData.put("start", pageNumber);
            jsonData.put("length", 15);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("JSON", jsonData.toString());
        return jsonData;
    }
}
