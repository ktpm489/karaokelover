package vn.com.frankle.karaokelover.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by duclm on 9/18/2016.
 */

public class KSharedPreference {

    public static final String PREFS_KARAOKETUBE = "KARAOKE_TUBE";

    public static final String KEY_FAVOURITES = "karaoke_favourite";

    public KSharedPreference() {
        super();
    }

    /**
     * Store list of favorites video's id to SharedPreference
     *
     * @param context   : context
     * @param favorites : list of favourite's video id
     */
    @SuppressLint("CommitPrefEdits")
    public void saveFavorite(Context context, List<String> favorites) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KARAOKETUBE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavourites = gson.toJson(favorites);

        editor.putString(KEY_FAVOURITES, jsonFavourites);
        editor.commit();
    }

    /**
     * Add a video's id to list of favorite video
     *
     * @param context         : context of activity
     * @param favoriteVideoId : video's id to be added
     */
    public void addFavorites(Context context, String favoriteVideoId) {
        ArrayList<String> curFavoritess = getFavoritesVideo(context);
        curFavoritess.add(favoriteVideoId);
        saveFavorite(context, curFavoritess);
    }

    /**
     * Remove a favorite video from list of favorite videos
     *
     * @param context      : context of activity
     * @param removedVideo : the video's id to be removed
     */
    public void removeFavorite(Context context, String removedVideo) {
        ArrayList<String> curFavorites = getFavoritesVideo(context);
        curFavorites.remove(removedVideo);
        saveFavorite(context, curFavorites);
    }

    /**
     * Get list of favorites video's id
     *
     * @param context : context
     * @return list of favorite video, empty list if there is no favorite video
     */
    public ArrayList<String> getFavoritesVideo(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KARAOKETUBE, Context.MODE_PRIVATE);
        ArrayList<String> favorites;
        if (settings.contains(KEY_FAVOURITES)) {
            String jsonFavorites = settings.getString(KEY_FAVOURITES, "");
            Gson gson = new Gson();
            String[] strFavorites = gson.fromJson(jsonFavorites, String[].class);
            favorites = new ArrayList<>(Arrays.asList(strFavorites));
        } else {
            return new ArrayList<>();
        }

        return favorites;
    }

    /**
     * Check if a video is in the favorite list or not
     *
     * @param videoId : id of video to be checked
     * @return true if this video is in favorite list
     */
    public boolean isInFavoriteList(Context context, String videoId) {
        ArrayList<String> listFavorite = getFavoritesVideo(context);
        return listFavorite.contains(videoId);
    }
}
