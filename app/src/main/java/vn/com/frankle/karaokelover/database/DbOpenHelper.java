package vn.com.frankle.karaokelover.database;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DbOpenHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "karaoke_lover.sqlite";
    private static final int DATABASE_VERSION = 1;

    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
