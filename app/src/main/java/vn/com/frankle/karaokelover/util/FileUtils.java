package vn.com.frankle.karaokelover.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by duclm on 10/18/2016.
 */

public class FileUtils {


    public static String loadDefaultDataJSON(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("default.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
