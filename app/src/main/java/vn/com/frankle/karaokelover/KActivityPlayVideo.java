package vn.com.frankle.karaokelover;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import vn.com.frankle.karaokelover.events.EventPrepareRecordingCountdown;
import vn.com.frankle.karaokelover.util.Utils;

public class KActivityPlayVideo extends AppCompatActivity implements KAudioRecord.AudioRecordListener {

    private static final String DEBUG_TAG = KActivityPlayVideo.class.getSimpleName();

    @NonNull
    private final CompositeSubscription compositeSubscriptionForOnStop = new CompositeSubscription();

    @BindView(R.id.content_kactivity_play_video)
    RelativeLayout mContentLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btn_recording)
    ImageButton btnRecord;
    @BindView(R.id.layout_ready)
    RelativeLayout mLayoutCountdown;
    @BindView(R.id.tv_countdown)
    TextView mTvCountdown;
    @BindView(R.id.btn_re_record)
    ImageButton btnReRecord;
    @BindView(R.id.timer_recording)
    TextView mTvTimerRecord;
    @BindView(R.id.saved_filename)
    TextView mTvSavedFilename;

    //    private GLAudioVisualizationView mAudioRecordVisualization;
    private String mCurrentVideoId;
    private String mCurrentVideoTitle;
    private String mCurrentSavedFilename;

    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private KAudioRecord mRecorder;
//    private KAudioRecordDbmHandler mAudioDbmHandler = new KAudioRecordDbmHandler();

    // Handling record timer
    private int recorderSecondsElapsed = 0;
    private Handler handler = new Handler();

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

    private View.OnClickListener onRecordClickListener = view -> onRecordButtonClick();

    private void switchRecordButton(boolean recording) {
        if (recording) {
            btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_stop_record));
        } else {
            btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_recording));
        }
    }

    /**
     * Handle record button click event
     */
    private void onRecordButtonClick() {
        if (!mRecorder.mIsRecording.get()) {
            Log.d(DEBUG_TAG, "Start recording....");
            new PrepareRecordingTask().execute();
        } else {
            Log.d(DEBUG_TAG, "Stop recording...");
            switchRecordButton(false);
            mRecorder.stop();
            mYoutubePlayer.pause();
//            mAudioDbmHandler.stopVisualizer();
            stopRecordingTimer();
            buildPostRecordDialog();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kplay_video);

        ButterKnife.bind(this);

        // Initialzie recorder
        mRecorder = new KAudioRecord(this);

        setupViews();
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

        btnRecord.setOnClickListener(onRecordClickListener);

//        mAudioRecordVisualization = new GLAudioVisualizationView.Builder(this)
//                .setLayersCount(1)
//                .setWavesCount(6)
//                .setWavesHeight(R.dimen.wave_height)
//                .setWavesFooterHeight(R.dimen.footer_height)
//                .setBubblesPerLayer(20)
//                .setBubblesSize(R.dimen.bubble_size)
//                .setBubblesRandomizeSize(true)
//                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
//                .setLayerColors(new int[]{ContextCompat.getColor(this, R.color.colorAccentBlur)})
//                .build();
//        mAudioRecordVisualization.linkTo(mAudioDbmHandler);
//
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        layoutParams.addRule(RelativeLayout.BELOW, R.id.youtube_player);
//        layoutParams.addRule(RelativeLayout.SYSTEM_UI_FLAG_VISIBLE, View.INVISIBLE);
//        mContentLayout.addView(mAudioRecordVisualization, 1, layoutParams);
    }

    /**
     * Build a dialog that appear when Stop recording button is clicked
     */
    private void buildPostRecordDialog() {
        AlertDialog.Builder postRecDialgBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View actionDialog = inflater.inflate(R.layout.dialog_post_recording, null);
        postRecDialgBuilder.setView(actionDialog);
        LinearLayout actionEditRecord = (LinearLayout) actionDialog.findViewById(R.id.action_edit_recording);
        actionEditRecord.setOnClickListener(view -> {
            Intent editRecordingIntent = new Intent(KActivityPlayVideo.this, KActivityEditRecording.class);
            editRecordingIntent.putExtra("TITLE", mCurrentSavedFilename);
            startActivity(editRecordingIntent);

            KActivityPlayVideo.this.finish();
        });
        postRecDialgBuilder.create().show();
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
        return super.onOptionsItemSelected(item);
    }

//    /**
//     * Preparing audio file (download from youtubeinmp3.com) of current Youtube video
//     */
//    private void prepareAudioFile() {
//        Intent intentDownloadAudio = new Intent(this, YoutubeAudioDownloadService.class);
//        intentDownloadAudio.putExtra("videoId", mCurrentVideoId);
//        intentDownloadAudio.putExtra("title", mCurrentVideoTitle);
//        startService(intentDownloadAudio);
//    }

//    private void showPreparingDownloadDialog() {
//        mProgressPrepare = new ProgressDialog(KActivityPlayVideo.this);
//        mProgressPrepare.setMessage("Preparing beat file...");
//        mProgressPrepare.show();
//    }

//    /**
//     * Show a progress dialog when starting downloading audio file
//     */
//    private void showDownloadProgressDialog() {
//        if (mProgressPrepare.isShowing()) {
//            mProgressPrepare.dismiss();
//        }
//        mProgressDownloadDialog = new ProgressDialog(KActivityPlayVideo.this);
//        mProgressDownloadDialog.setIndeterminate(false);
//        mProgressDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mProgressDownloadDialog.setCancelable(false);
//        mProgressDownloadDialog.setMax(100);
//        mProgressDownloadDialog.setTitle("Downloading beat file");
//        mProgressDownloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> {
//        });
//        mProgressDownloadDialog.show();
//    }

//    /**
//     * Event : preparing to download beat file (waiting for server to convert youtube video into mp3 file)
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventPreparingDownloadAudio(EventDownloadAudioPreparing event) {
//        showPreparingDownloadDialog();
//    }

//    /**
//     * Event : conversion is completed. Beat file is ready to be downloaded.
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventStartDownloadAudio(EventDownloadAudioStart event) {
//        showDownloadProgressDialog();
//    }

//    /**
//     * Receive event download progress -> update progress dialog
//     *
//     * @param event : event object contains information about progress
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventDownloadProgress(EventDownloadAudioProgress event) {
//        mProgressDownloadDialog.setProgress(event.getProgress());
//        if (event.getProgress() < 100) {
//            mProgressDownloadDialog.setTitle(String.format(Locale.ENGLISH, "Downloaded (%d/%d) MB", event.getDownloadedSize(), event.getTotalFileSize()));
//        }
//    }
//
//    /**
//     * Event: download beat file completed, start recording user's voice
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventDownloadAudioCompleted(EventDownloadAudioCompleted event) {
//        mProgressDownloadDialog.setProgress(100);
//        mProgressDownloadDialog.setTitle("Finished preparing beat file successfully.");
//        mProgressDownloadDialog.dismiss();
//    }
//
//    /**
//     * Event indicate that has to load a webview to download beat file
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventDownloadAudioError(EventDownloadAudioError event) {
//        mProgressDownloadDialog.dismiss();
//        new AlertDialog.Builder(this)
//                .setTitle("Error")
//                .setMessage("Internal problem when preparing beat file. Please try again!")
//                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                .show();
//    }

    /**
     * Event of running prepare recording countdown
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventPrepareCountdownRunning(EventPrepareRecordingCountdown event) {
        int current = event.getCurrentValue();
        Log.d(DEBUG_TAG, "Countdown: value = " + current);

        if (current == 0) {
            mTvCountdown.setText("READY!!!");
        } else {
            mTvCountdown.setText(String.valueOf(current));
        }
    }

    /**
     * Task to update timer
     */
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> mTvTimerRecord.setText(Utils.formatSeconds(recorderSecondsElapsed)));
            recorderSecondsElapsed++;
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * Start updating recording timer
     */
    private void startRecordingTimer() {
        handler.post(updateTimer);
    }

    /**
     * Stop updating recording timer
     */
    private void stopRecordingTimer() {
        handler.removeCallbacks(updateTimer);
    }

    /**
     * Callback to get sample when recording
     *
     * @param data : sample data
     */
    @Override
    public void onAudioRecordDataReceived(byte[] data) {
        /* TO-DO: handle recording sample data */
    }

    /**
     * Callback when recording get errors
     */
    @Override
    public void onAudioRecordError() {

    }


    /**
     * Display a prepare screen before starting recording
     */
    private class PrepareRecordingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mYoutubePlayer.isPlaying()) {
                mYoutubePlayer.pause();
                mYoutubePlayer.seekToMillis(0);
            }
            mLayoutCountdown.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 3; i >= 0; --i) {
                Log.d(DEBUG_TAG, "Starting countdown: current = " + i);
                KApplication.eventBus.post(new EventPrepareRecordingCountdown(i));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mLayoutCountdown.setVisibility(View.INVISIBLE);
            // Start recording voice
            startRecording();
        }
    }

    /**
     * Start recording voice
     */
    private void startRecording() {
        if (mYoutubePlayer != null && !mYoutubePlayer.isPlaying()) {
            mCurrentSavedFilename = Utils.getAutoFilename();
            switchRecordButton(true);

            /*Auto configure audio output volume (try to prevent too loud beat)*/
            configAudioVolume();

            mYoutubePlayer.play();
            mTvTimerRecord.setVisibility(View.VISIBLE);
            mTvSavedFilename.setText(mCurrentSavedFilename);
            mTvSavedFilename.setVisibility(View.VISIBLE);
//            mAudioRecordVisualization.setVisibility(View.VISIBLE);

            mRecorder.start(mCurrentSavedFilename);
            startRecordingTimer();
        }
    }

    /**
     * If user record directly, this lead to a problem that the beat volume is too loud and user's
     * voice volume is too low -> pre-configure audio volume, user may change later if they find it's
     * not suitable
     */
    private void configAudioVolume() {
        // This value's result maybe vary between devices
        // Just a start number, user may change volume if it's too loud or too small
        float percent = 0.4f;

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int autoVol = (int) (maxVol * percent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, autoVol, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mAudioRecordVisualization.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mAudioRecordVisualization.onResume();
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
//        mAudioRecordVisualization.release();
    }
}
