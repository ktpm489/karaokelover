package vn.com.frankle.karaokelover.zingmp3;

import android.util.Base64;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import vn.com.frankle.karaokelover.util.JSONHelper;
import vn.com.frankle.karaokelover.util.Utils;

public class ZingMp3API {

    private static final String PUBLIC_KEY = "faf55f7e16a5218e171bd781c8f21b8d540ce170";
    private static final String PRIVATE_KEY = "2a52c25134a13473e1d86c53676ca4e2";

    private static final String URL_LIST_ARTIST = "http://api.mp3.zing.vn/api/mobile/artist/getartistbygenre";
    private static final String URL_DETAIL = "http://api.mp3.zing.vn/api/detail";
    private static final String URL_ARTIST_INFO = "http://api.mp3.zing.vn/api/singer-info";

    private static final String URL_AVATAR_IMG = "http://image.mp3.zdn.vn/thumb/240_240/";

    /**
     * Get the full URL to send to Zing MP3 server
     *
     * @param actionURL : type of action
     * @param JSONData  : Json data for the action
     * @return URL for sending request to ZingMP3 server
     * @throws UnsupportedEncodingException
     */
    private static String getZingMp3RequestURL(String actionURL, JSONObject JSONData)
            throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder(actionURL);
        url.append("?publicKey=");
        url.append(PUBLIC_KEY);
        url.append("&signature=");
        String encodedJSONData = URLEncoder
                .encode(Base64.encodeToString(
                        JSONData.toString().getBytes("UTF-8"), Base64.DEFAULT),
                        "UTF-8"
                );
        String signature = Utils.strToHMACMD5(encodedJSONData, PRIVATE_KEY);
        url.append(signature);
        url.append("&jsondata=");
        url.append(encodedJSONData);
        return url.toString();
    }

    public static String getListArtistURL()
            throws UnsupportedEncodingException {
        return getZingMp3RequestURL(URL_LIST_ARTIST, JSONHelper.writeJsonDataArtists(0));
    }

    public static String getDetailContentURL(String typeContent, String id)
            throws UnsupportedEncodingException {
        JSONObject jsonData = JSONHelper.writeJSONDataDetail(typeContent, id);
        return getZingMp3RequestURL(URL_DETAIL, jsonData);
    }

    public static String getArtistInfoURL(String id)
            throws UnsupportedEncodingException {
        JSONObject jsonData = JSONHelper.writeJSONDataArtistInfo(id);
        return getZingMp3RequestURL(URL_ARTIST_INFO, jsonData);
    }

    public static String getArtistAvatarURL(String artistUrl) {
        return URL_AVATAR_IMG + artistUrl;
    }
}
