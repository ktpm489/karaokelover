package vn.com.frankle.karaokelover;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by duclm on 9/18/2016.
 */

public class KSharedPreference {

    private static final String PREFS_KARAOKETUBE = "KARAOKE_TUBE";

    private static final String KEY_FAVOURITES = "karaoke_favourite";
    private static final String KEY_FAVOURITE_ARTISTS = "karaoke_favourite_artist";
    private static final String KEY_RELOAD_FAVORITE_LIST_FLAG = "karaoke_reload_favorite_list";
    private static final String KEY_RECORDING_PROGRESS = "karaoke_recording_progress";
    private static final String KEY_RECORDING_VIDEOID = "karaoke_recording_videoid";

    private String defaultFavoriteArtists[] = {"Backstreet Boys", "Trung Quân Idol", "Sơn Tùng MTP"};
    private final Set<String> DEFAULT_FAVORITE_ARTISTS = new HashSet<>(Arrays.asList(defaultFavoriteArtists));

    private Context mContext;

    public KSharedPreference(Context context) {
        super();
        this.mContext = context;
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
    private ArrayList<String> getFavoritesVideo(Context context) {
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


    /**
     * Get list of favourite artists
     */
    public ArrayList<String> getFavouriteArtists(Context context) {
        SharedPreferences sf = context.getSharedPreferences(PREFS_KARAOKETUBE, Context.MODE_PRIVATE);

        ArrayList<String> favouriteArtists = new ArrayList<>();

        if (sf.contains(KEY_FAVOURITE_ARTISTS)) {
            Set<String> artistsSet = sf.getStringSet(KEY_FAVOURITE_ARTISTS, DEFAULT_FAVORITE_ARTISTS);
            favouriteArtists.addAll(artistsSet);
        } else {
            favouriteArtists.addAll(DEFAULT_FAVORITE_ARTISTS);
        }

        return favouriteArtists;
    }

    public boolean getFavoriteListReloadFlag() {
        SharedPreferences sf = mContext.getSharedPreferences(PREFS_KARAOKETUBE, Context.MODE_PRIVATE);
        return sf.getBoolean(KEY_RELOAD_FAVORITE_LIST_FLAG, false);
    }

    public void setFavoriteListReloadFlag(boolean needReload) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_KARAOKETUBE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(KEY_RELOAD_FAVORITE_LIST_FLAG, needReload);
        editor.commit();
    }
}
