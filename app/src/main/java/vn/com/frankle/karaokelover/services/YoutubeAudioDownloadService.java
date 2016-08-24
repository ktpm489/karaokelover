package vn.com.frankle.karaokelover.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.events.EventDownloadAudioCompleted;
import vn.com.frankle.karaokelover.events.EventDownloadAudioError;
import vn.com.frankle.karaokelover.events.EventDownloadAudioPreparing;
import vn.com.frankle.karaokelover.events.EventDownloadAudioProgress;
import vn.com.frankle.karaokelover.events.EventDownloadAudioStart;
import vn.com.frankle.karaokelover.models.YoutubeAudioDownload;

/**
 * Created by duclm on 8/16/2016.
 */

public class YoutubeAudioDownloadService extends IntentService {
    private static final String TAG = YoutubeAudioDownloadService.class.getSimpleName();

    private String mCurrentVideoId;
    private String mVideoTitle;

    public YoutubeAudioDownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get current video id
        mCurrentVideoId = intent.getStringExtra("videoId");
        mVideoTitle = intent.getStringExtra("title");

        KApplication.eventBus.post(new EventDownloadAudioPreparing());

        new Thread(() -> {
            int retry = 0;
            do {
                final String audioUrl = getAudioUrl();
                if (null == audioUrl) {
                    //TO-DO
                    //Handle error getting audio url
                    return;
                }

                Call<ResponseBody> requestAudioUrl = KApplication.getYoutubeInMp3APIService().downloadAudioFile(audioUrl.substring(16));
                try {
                    ResponseBody audioContent = requestAudioUrl.execute().body();
                    if (audioContent.contentLength() < 0) {
                        // Delay thread for 7 seconds before retrying to get audio file
                        Log.d(TAG, "Retry getting beat file form youtubeinmp3 server");
                        Thread.sleep(5000);
                        ++retry;
                    } else {
                        Log.d(TAG, "Successful retry. Start downloading...");
                        KApplication.eventBus.post(new EventDownloadAudioStart());
                        downloadAudioFile(audioContent);
                        return;
                    }
                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "Exception when getting beat file");
                    e.printStackTrace();
                }
                Log.e(TAG, "Error get audio file from youtubeinmp3.com server");
            }while (retry < 4);
            KApplication.eventBus.post(new EventDownloadAudioError());
        }).run();
    }

    /**
     * Get audio url of current Youtube video from youtubeinmp3.com
     *
     * @return url of audio or null if error
     */
    @Nullable
    private String getAudioUrl() {
        try {
            Document doc = Jsoup.connect(UrlHelper.getYoutubeInMp3Url(mCurrentVideoId)).get();
            Elements downloadElements = doc.select("body").select("section").select("div[class=infoBox]").select("p[class=download-buttons fullWidth]").select("a[id=download]");
            for (Element element : downloadElements) {
                String url = element.attr("href");
                Log.d(TAG, "audio url = " + url);
                return url;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while getting audio url from youtubeinmp3.com");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Download audio file from youtubeinmp3.com
     */
    private void downloadAudioFile(ResponseBody body) throws IOException {
        if (body == null) {
            Log.e(TAG, "Error getting file form server: null ResponseBody");
            return;
        }

        int count;
        int totalFileSize;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        Log.d(TAG, "Filesize = " + fileSize + " bytes");
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File downloadFileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke Lover/Beats/");
        if (!downloadFileDir.exists()) {
            downloadFileDir.mkdirs();
        }

        File downloadFile = new File(downloadFileDir, mVideoTitle + ".mp3");
        OutputStream output = new FileOutputStream(downloadFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            YoutubeAudioDownload download = new YoutubeAudioDownload();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                // Notify about download progress
                KApplication.eventBus.post(new EventDownloadAudioProgress(download));
                Log.d(TAG, "Update download progress dialog");
                timeCount++;
            }

            output.write(data, 0, count);
        }
        // Notify on download completed
        KApplication.eventBus.post(new EventDownloadAudioCompleted());

        output.flush();
        output.close();
        bis.close();
    }
}
