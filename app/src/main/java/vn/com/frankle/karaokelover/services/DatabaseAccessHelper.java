package vn.com.frankle.karaokelover.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import vn.com.frankle.karaokelover.models.LocalArtist;

/**
 * Created by duclm on 7/24/2016.
 */

public class DatabaseAccessHelper {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccessHelper instance;

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccessHelper(Context context) {
        this.openHelper = new KaraokeDatabase(context);
    }

    /**
     * Return a singleton instance of DatabaseAccessHelper.
     *
     * @param context the Context
     * @return the instance of DatabaseAccessHelper
     */
    public static DatabaseAccessHelper getInstance(Context context){
        if (instance == null){
            instance = new DatabaseAccessHelper(context);
        }
        return instance;
    }

    /**
     * Open the database connection. This will make the database available if it's the first run
     */
    public void openDbConnection() {
        this.database = openHelper.getReadableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void closeDbConnection() {
        if (database != null) {
            this.database.close();
        }
    }

    public List<LocalArtist> getListArtists(){
        LocalArtist item = null;
        List<LocalArtist> listArtists = new ArrayList<>();
        openDbConnection();
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_artist", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            item = new LocalArtist(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
            listArtists.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        closeDbConnection();
        return listArtists;
    }

    public List<LocalArtist> getKaraokeOfArtist(int artistID){
        LocalArtist item = null;
        List<LocalArtist> listArtists = new ArrayList<>();
        openDbConnection();
        Cursor cursor = database.rawQuery("SELECT * FROM tbl_artist", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            item = new LocalArtist(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
            listArtists.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        closeDbConnection();
        return listArtists;
    }
}
