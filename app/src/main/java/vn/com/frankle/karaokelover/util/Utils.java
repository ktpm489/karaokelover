package vn.com.frankle.karaokelover.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.models.Thumbnails;

/**
 * Created by duclm on 7/13/2016.
 */

public class Utils {
    private static final String TAG = "KaraokeLover";
    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1000, "K");
        suffixes.put(1000000, "M");
        suffixes.put(1000000000, "B");
    }

    public static void printLog(String tag, String msg) {
        Log.i(TAG + "-" + tag, msg);
    }

    public static int convertDpToPixel(Context context, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue, context.getResources().getDisplayMetrics());
    }

    private static String formatAbbreviationNumber(int value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Integer.MIN_VALUE) return formatAbbreviationNumber(Integer.MIN_VALUE + 1);
        if (value < 1000) return Integer.toString(value); //deal with easy case

        Map.Entry<Integer, String> e = suffixes.floorEntry(value);
        Integer divideBy = e.getKey();
        String suffix = e.getValue();

        int truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    /**
     * Get short-format view count of Youtube video
     *
     * @param viewcount : viewcount of the video in String format
     * @return short-format viewcount string
     */
    public static String getViewCount(String viewcount) {
        if (viewcount == null || viewcount.isEmpty() || viewcount.equals("0")) {
            return "0 play";
        }
        int original_viewcount = Integer.parseInt(viewcount);
        String abbreViewCount = formatAbbreviationNumber(original_viewcount);
        return abbreViewCount + " plays";
    }

    /**
     * Get short-format like count of Youtube video
     *
     * @param likecount : likecount of the video in String format
     * @return short-format likecount string
     */
    public static String getLikeCount(String likecount) {
        if (likecount == null || likecount.isEmpty() || likecount.equals("0")) {
            return "0 like";
        }
        int original_likecount = Integer.parseInt(likecount);
        String abbreLikeCount = formatAbbreviationNumber(original_likecount);
        return abbreLikeCount + " likes";
    }

    /**
     * Get video thumbnail from response of Youtube API
     *
     * @param thumbnail : Youtube server response result on video thumbnail
     * @return url of video thumbnail
     */
    public static String getThumbnailURL(Thumbnails thumbnail) {
        if (thumbnail.getMaxres() != null) {
            return thumbnail.getMaxres().getUrl();
        }
        if (thumbnail.getHigh() != null) {
            return thumbnail.getHigh().getUrl();
        }
        if (thumbnail.getMedium() != null) {
            return thumbnail.getMedium().getUrl();
        }
        return thumbnail.getDefault().getUrl();
    }

    /**
     * Convert Youtube time format (ISO8601) to format of mm:ss
     *
     * @param time : Youtube time format
     * @return human-readable time format
     */
    public static String convertYoutubeTimeformat(String time) {
        PeriodFormatter formatterISO = ISOPeriodFormat.standard();
        Period period = formatterISO.parsePeriod(time).normalizedStandard();

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .printZeroAlways().minimumPrintedDigits(2).appendMinutes()
                .appendSeparator(":")
                .printZeroAlways().minimumPrintedDigits(2).appendSeconds()
                .toFormatter();
        return formatter.print(period);
    }

    /**
     * Break a list of strings into multiple lists of given max size partitionSize
     *
     * @param originalList  the original list
     * @param partitionSize max number of elements per list
     * @return a list of paritioned lists
     */
    public static List<List> partition(List originalList, int partitionSize) {
        List<List> partitions = new LinkedList<List>();
        for (int i = 0; i < originalList.size(); i += partitionSize) {
            partitions.add(originalList.subList(i, i + Math.min(partitionSize, originalList.size() - i)));
        }
        return partitions;
    }

    private static String getTwoDecimalsValue(int value) {
        if (value >= 0 && value <= 9) {
            return "0" + value;
        } else {
            return value + "";
        }
    }

    public static String formatSeconds(int seconds) {
        return getTwoDecimalsValue(seconds / 60) + ":"
                + getTwoDecimalsValue(seconds % 60);
    }

    public static final String getAutoFilename() {
        return new SimpleDateFormat("yyyyMMdd'_'HHmmss'.wav'").format(new Date());
    }

    public static final String getFilenameExcludeExtension(String fullFilename) {
        return fullFilename.replaceFirst("[.][^.]+$", "");
    }

    public static String formatDuration(long milliseconds) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static final String getDuration(String pathFile) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(pathFile);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return formatDuration(Long.parseLong(durationStr));
    }

    /**
     * String to HMAC - MD5
     *
     * @param s         : encoded Json data
     * @param keyString : private key from Zing
     * @return signature for building URL
     */
    public static String strToHMACMD5(String s, String keyString) {
        String sEncodedString = null;
        try {
            SecretKeySpec key = new SecretKeySpec(
                    (keyString).getBytes("UTF-8"), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);

            byte[] bytes = mac.doFinal(s.getBytes("ASCII"));

            StringBuilder hash = new StringBuilder();

            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            sEncodedString = hash.toString();
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sEncodedString;
    }

    /**
     * Check if a filename in Recording File directory is available or not
     *
     * @param filename : filename to be checked
     * @return true : if this filename is avai
     */
    public static boolean isAvailableFilename(String filename) {
        File recordFileDir = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
        if (!recordFileDir.exists()) {
            return true;
        }
        File recordFile = new File(recordFileDir, filename);
        return !recordFile.exists();
    }

    /**
     * Check if device's network is available
     *
     * @param context : context
     * @return true : if device is connecting to the internet
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
