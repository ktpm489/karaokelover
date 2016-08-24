package vn.com.frankle.karaokelover;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.events.EventDownloadAudioCompleted;
import vn.com.frankle.karaokelover.events.EventDownloadAudioError;
import vn.com.frankle.karaokelover.events.EventDownloadAudioPreparing;
import vn.com.frankle.karaokelover.events.EventDownloadAudioProgress;
import vn.com.frankle.karaokelover.events.EventDownloadAudioStart;
import vn.com.frankle.karaokelover.services.YoutubeAudioDownloadService;

public class KActivityPlayVideo extends AppCompatActivity {

    private static final String DEBUG_TAG = KActivityPlayVideo.class.getSimpleName();

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btn_stop_recording)
    Button btnStopRecording;

    private String mCurrentVideoId;
    private String mCurrentVideoTitle;

    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private KAudioRecord mRecorder;
    private ProgressDialog mProgressDownloadDialog;
    private ProgressDialog mProgressPrepare;

    private YouTubePlayer mYoutubePlayer;

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onLoading");
        }

        @Override
        public void onLoaded(String s) {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onLoaded");
        }

        @Override
        public void onAdStarted() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onAdStared");
        }

        @Override
        public void onVideoStarted() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnVideoStarted");
        }

        @Override
        public void onVideoEnded() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnVideoEnded");
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnError");
        }
    };

    private YouTubePlayer.PlaybackEventListener mPlaybackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.OnInitializedListener onInitializedListener = new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
            mYoutubePlayer = youTubePlayer;
            mYoutubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
            mYoutubePlayer.setPlaybackEventListener(mPlaybackEventListener);
            mYoutubePlayer.loadVideo(mCurrentVideoId);
            mYoutubePlayer.play();
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kplay_video);

        ButterKnife.bind(this);

        setupViews();

        // Initialzie recorder
        mRecorder = new KAudioRecord(new KAudioRecord.AudioRecordListener() {
            @Override
            public void onAudioRecordDataReceived(byte[] data) {
            }

            @Override
            public void onError() {

            }
        });
    }

    /**
     * Initialize activity's view on create
     */
    private void setupViews() {
        mCurrentVideoTitle = getIntent().getStringExtra("title");
        mCurrentVideoId = getIntent().getStringExtra("videoid");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mCurrentVideoTitle);

        //Setup youtube player view
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_player);
        mYoutubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, onInitializedListener);

        btnStopRecording.setOnClickListener(view -> {
            if (mYoutubePlayer != null) {
                if (mYoutubePlayer.isPlaying()) {
                    mYoutubePlayer.pause();
                } else {
                    mYoutubePlayer.seekToMillis(0);
                    mYoutubePlayer.play();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kactivity_play_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_record) {
            startRecording();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Preparing audio file (download from youtubeinmp3.com) of current Youtube video
     */
    private void prepareAudioFile() {
        Intent intentDownloadAudio = new Intent(this, YoutubeAudioDownloadService.class);
        intentDownloadAudio.putExtra("videoId", mCurrentVideoId);
        intentDownloadAudio.putExtra("title", mCurrentVideoTitle);
        startService(intentDownloadAudio);
    }

    private void showPreparingDownloadDialog() {
        mProgressPrepare = new ProgressDialog(KActivityPlayVideo.this);
        mProgressPrepare.setMessage("Preparing beat file...");
        mProgressPrepare.show();
    }

    /**
     * Show a progress dialog when starting downloading audio file
     */
    private void showDownloadProgressDialog() {
        if (mProgressPrepare.isShowing()) {
            mProgressPrepare.dismiss();
        }
        mProgressDownloadDialog = new ProgressDialog(KActivityPlayVideo.this);
        mProgressDownloadDialog.setIndeterminate(false);
        mProgressDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDownloadDialog.setCancelable(false);
        mProgressDownloadDialog.setMax(100);
        mProgressDownloadDialog.setTitle("Downloading beat file");
        mProgressDownloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> {
        });
        mProgressDownloadDialog.show();
    }

    /**
     * Event : preparing to download beat file (waiting for server to convert youtube video into mp3 file)
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventPreparingDownloadAudio(EventDownloadAudioPreparing event) {
        showPreparingDownloadDialog();
    }

    /**
     * Event : conversion is completed. Beat file is ready to be downloaded.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventStartDownloadAudio(EventDownloadAudioStart event) {
        showDownloadProgressDialog();
    }

    /**
     * Receive event download progress -> update progress dialog
     *
     * @param event : event object contains information about progress
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownloadProgress(EventDownloadAudioProgress event) {
        mProgressDownloadDialog.setProgress(event.getProgress());
        if (event.getProgress() < 100) {
            mProgressDownloadDialog.setTitle(String.format(Locale.ENGLISH, "Downloaded (%d/%d) MB", event.getDownloadedSize(), event.getTotalFileSize()));
        }
    }

    /**
     * Event: download beat file completed, start recording user's voice
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownloadAudioCompleted(EventDownloadAudioCompleted event) {
        mProgressDownloadDialog.setProgress(100);
        mProgressDownloadDialog.setTitle("Finished preparing beat file successfully.");
        mProgressDownloadDialog.dismiss();
    }

    /**
     * Event indicate that has to load a webview to download beat file
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownloadAudioError(EventDownloadAudioError event) {
        mProgressDownloadDialog.dismiss();
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Internal problem when preparing beat file. Please try again!")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    /**
     * Start recording voice
     */
    private void startRecording() {
        Toast.makeText(KActivityPlayVideo.this, "Start recording...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "OnStop-------------------------");
        KApplication.eventBus.unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "OnStart-------------------------");
        KApplication.eventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscriptionForOnStop.unsubscribe();
    }
}
