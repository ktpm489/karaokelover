package vn.com.frankle.karaokelover;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import vn.com.frankle.karaokelover.models.Thumbnails;

/**
 * Created by duclm on 7/13/2016.
 */

public class Utils {
    private static final String TAG = "KaraokeLover";

    public static void printLog(String tag, String msg) {
        Log.i(TAG +  "-" + tag, msg);
    }

    public static int convertDpToPixel(Context context, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue, context.getResources().getDisplayMetrics());
    }

    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1000, "K");
        suffixes.put(1000000, "M");
        suffixes.put(1000000000, "B");
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
}
