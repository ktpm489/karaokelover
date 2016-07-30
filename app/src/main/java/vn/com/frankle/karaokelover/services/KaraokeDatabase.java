package vn.com.frankle.karaokelover.services;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by duclm on 7/24/2016.
 */

public class KaraokeDatabase extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "karaoke_lover.sqlite";
    private static final int DATABASE_VERSION = 1;

    public KaraokeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
