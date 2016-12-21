package vn.com.frankle.karaokelover.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.orhanobut.logger.Logger
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_play_video.*
import kotlinx.android.synthetic.main.content_connection_error.*
import kotlinx.android.synthetic.main.content_error_loading.*
import kotlinx.android.synthetic.main.content_kactivity_play_video.*
import kotlinx.android.synthetic.main.content_no_comment.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import vn.com.frankle.karaokelover.*
import vn.com.frankle.karaokelover.activities.states.KActivityPlayVideoBaseState
import vn.com.frankle.karaokelover.activities.states.KActivityPlayVideoPlayingState
import vn.com.frankle.karaokelover.activities.states.KActivityPlayVideoRecordingState
import vn.com.frankle.karaokelover.activities.states.KActivityPlayVideoUnitializeState
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener
import vn.com.frankle.karaokelover.adapters.KAdapterComments
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener
import vn.com.frankle.karaokelover.adapters.viewholders.ViewHolderComment
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm
import vn.com.frankle.karaokelover.events.EventPrepareRecordingCountdown
import vn.com.frankle.karaokelover.events.EventUpdateFavoriteList
import vn.com.frankle.karaokelover.fragments.KYoutubePlayerFragment
import vn.com.frankle.karaokelover.services.responses.ResponseCommentThreads
import vn.com.frankle.karaokelover.services.responses.youtube.commentthread.CommentThread
import vn.com.frankle.karaokelover.util.Utils
import vn.com.frankle.karaokelover.views.recyclerview.InsetDividerDecoration
import java.io.File

class KActivityPlayVideo : AppCompatActivity(), KAudioRecord.AudioRecordListener {

    private val compositeSubscriptionForOnStop = CompositeSubscription()
    private val realm: Realm = Realm.getDefaultInstance()

    internal lateinit var onScrollListener: EndlessRecyclerViewScrollListener
    private var mAdapterComment: KAdapterComments? = null
    //    private GLAudioVisualizationView mAudioRecordVisualization;
    private lateinit var mCurrentVideoId: String
    private var mCurrentVideoTitle: String? = null
    private var mCurrentSavedFilename: String? = null
    private var mCurrentCommentPageToken: String? = null
    private var mFavoriteState: Boolean = false
    private lateinit var mYoutubePlayerFragment: KYoutubePlayerFragment
    // Store previously video's position
    private var mLastVideoPos: Int = 0
    private var mRecorder: KAudioRecord? = null
    // Handling record timer
    private var recorderSecondsElapsed = 0
    private val handler = Handler()
    private var mYoutubePlayer: YouTubePlayer? = null
    private val mSharedPref: KSharedPreference = KSharedPreference(this@KActivityPlayVideo)

    private lateinit var mState: KActivityPlayVideoBaseState
    // States of activity
    private val mUninitState: KActivityPlayVideoBaseState = KActivityPlayVideoUnitializeState(this@KActivityPlayVideo)
    private val mPlayingState: KActivityPlayVideoBaseState = KActivityPlayVideoPlayingState(this@KActivityPlayVideo)
    private val mRecordingState: KActivityPlayVideoBaseState = KActivityPlayVideoRecordingState(this@KActivityPlayVideo)

    //----------------------------------LIFE CYCLE--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)

        // Set activity to unitialize state
        setActivityState(mUninitState)

        // Initialzie recorder
        mRecorder = KAudioRecord(this)

        // Get extra data from received intent
        mCurrentVideoTitle = intent.getStringExtra("title")
        mCurrentVideoId = intent.getStringExtra("videoid")

        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = mCurrentVideoTitle

        layout_connection_error.setOnClickListener { checkInternetConnectionAndInitViews() }
        content_error_loading.setOnClickListener { loadVideoComments() }

        checkInternetConnectionAndInitViews()
    }

    override fun onStart() {
        super.onStart()
        Log.d(DEBUG_TAG, "onStart")
        KApplication.eventBus.register(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(DEBUG_TAG, "onResume")
        mState.onResume()
    }

    override fun onPause() {
        Log.d(DEBUG_TAG, "onPause")
        super.onPause()
        mState.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d(DEBUG_TAG, "onStop")
        KApplication.eventBus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscriptionForOnStop.unsubscribe()
    }

    //----------------------------------LIFE CYCLE--------------------------------------------------

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (RECOVERY_REQUEST == requestCode) {
            mYoutubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, onInitializedListener)
        }
    }

    private val playerStateChangeListener = object : YouTubePlayer.PlayerStateChangeListener {
        override fun onLoading() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onLoading")
        }

        override fun onLoaded(s: String) {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onLoaded")
        }

        override fun onAdStarted() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - onAdStared")
        }

        override fun onVideoStarted() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnVideoStarted")
        }

        override fun onVideoEnded() {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnVideoEnded")
            if (mRecorder != null && mRecorder!!.mIsRecording.get()) {
                // The application is recording
                stopRecording()
            }
        }

        override fun onError(errorReason: YouTubePlayer.ErrorReason) {
            Log.i(DEBUG_TAG, "YouTubePlayer.PlayerStateChangeListener - OnError: " + errorReason.name)
        }
    }
    private val mPlaybackEventListener = object : YouTubePlayer.PlaybackEventListener {
        override fun onPlaying() {
            Log.i(DEBUG_TAG, "PlaybackEventListener - onPlaying")
        }

        override fun onPaused() {
            Log.i(DEBUG_TAG, "PlaybackEventListener - onPaused")
            if (mRecorder != null && mRecorder!!.mIsRecording.get()) {
                // Currently is recording
                switchRecordButton(false)
                mRecorder!!.stop()
                stopRecordingTimer()
                buildPostRecordDialog()
            }
        }

        override fun onStopped() {
            Log.d(DEBUG_TAG, "PlaybackEventListener - onStopped")
        }

        override fun onBuffering(b: Boolean) {
            Log.d(DEBUG_TAG, "PlaybackEventListener - onBuffering")
        }

        override fun onSeekTo(i: Int) {
            Log.d(DEBUG_TAG, "PlaybackEventListener - onSeekTo " + i)
        }


    }
    private val onInitializedListener = object : YouTubePlayer.OnInitializedListener {
        override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, wasRestored: Boolean) {
            Log.d(DEBUG_TAG, "YoutubePlayer - onInitializationSuccess")
            mYoutubePlayer = youTubePlayer
            mYoutubePlayer!!.setPlayerStateChangeListener(playerStateChangeListener)
            mYoutubePlayer!!.setPlaybackEventListener(mPlaybackEventListener)
            mYoutubePlayer!!.setShowFullscreenButton(false)
            if (!wasRestored) {
                mYoutubePlayer!!.loadVideo(mCurrentVideoId)
                setActivityState(mPlayingState)
            } else {
                Log.d(DEBUG_TAG, "Restored from a previously saved state")
            }
        }

        override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {
            if (youTubeInitializationResult.isUserRecoverableError) {
                youTubeInitializationResult.getErrorDialog(this@KActivityPlayVideo, 1).show()
            } else {
                Toast.makeText(this@KActivityPlayVideo,
                        "Failed to initialize video, please try again!",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Used for re-initialization of youtube player
     */
    private val onReInitializedListener = object : YouTubePlayer.OnInitializedListener {
        override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, wasRestored: Boolean) {
            Log.d(DEBUG_TAG, "YoutubePlayer - onReInitializationSuccess")
            if (!wasRestored) {
                mYoutubePlayer = youTubePlayer
                mYoutubePlayer!!.setPlayerStateChangeListener(playerStateChangeListener)
                mYoutubePlayer!!.setPlaybackEventListener(mPlaybackEventListener)
                mYoutubePlayer!!.setShowFullscreenButton(false)
                mYoutubePlayer!!.cueVideo(mCurrentVideoId, mLastVideoPos)
            }
        }

        override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {
            Log.d(DEBUG_TAG, "YoutubePlayer - onInitializationFailure")
        }
    }

    /**
     * Callback to get sample when recording

     * @param data : sample data
     */
    override fun onAudioRecordDataReceived(data: ByteArray?, readSize: Int) {
//        runOnUiThread { visualizer!!.addAmplitude(AudioChunk.getMaxAmplitude(data, readSize)) }
    }

    /**
     * Callback when recording get errors
     */
    override fun onAudioRecordError() {

    }

    fun setActivityState(newState: KActivityPlayVideoBaseState) {
        this.mState = newState
    }

    fun getYoutubePlayerInstance(): YouTubePlayer? {
        return this.mYoutubePlayer
    }

    /**
     * Task to update timer
     */
    private val updateTimer = object : Runnable {
        override fun run() {
            runOnUiThread { timer_recording!!.text = Utils.formatSeconds(recorderSecondsElapsed) }
            recorderSecondsElapsed++
            handler.postDelayed(this, 1000)
        }
    }

    private fun switchRecordButton(recording: Boolean) {
        if (recording) {
            btn_recording!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_stop_record))
        } else {
            btn_recording!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_recording))
        }
    }

    /**
     * Handle record button click event
     */
    private fun onRecordButtonClick() {
        if (!mRecorder!!.mIsRecording.get()) {
            mCurrentSavedFilename = mCurrentVideoTitle!! + ".wav"

            if (!Utils.isAvailableFilename(mCurrentSavedFilename)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(resources.getString(R.string.msg_filename_existed))
                builder.setNegativeButton("NO") { dialog, which -> dialog.cancel() }
                builder.setPositiveButton("YES") { dialog, which -> DeleteRecordedFileTask().execute() }
                builder.create().show()
            } else {
                Log.d(DEBUG_TAG, "Start preparing recording task")
                PrepareRecordingTask().execute()
            }
        } else {
            Log.d(DEBUG_TAG, "Stop recording...")
            switchRecordButton(false)
            mRecorder!!.stop()
            stopRecordingTimer()
            buildPostRecordDialog()
        }
    }

    /**
     * Setup views based on validity of internet connection
     */
    private fun checkInternetConnectionAndInitViews() {
        if (Utils.isOnline(this@KActivityPlayVideo)) {
            setLayoutVisibility(LayoutType.PLAYING)
            setupViews()
            loadVideoComments()
        } else {
            setLayoutVisibility(LayoutType.ERROR_NO_CONNECTION)
        }
    }

    private fun loadVideoComments() {
        setPlayViewVisibility(ContentViewtype.PROGRESSBAR)
        val obsComment = KApplication.rxYoutubeAPIService.getVideoComments(mCurrentVideoId!!)
        compositeSubscriptionForOnStop.add(obsComment.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<ResponseCommentThreads>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        setPlayViewVisibility(ContentViewtype.ERROR_LOAD_COMMENT)
                    }

                    override fun onNext(responseCommentThreads: ResponseCommentThreads) {
                        handleCommentResults(responseCommentThreads)
                    }
                }))
    }

    private fun handleCommentResults(responseCommentThreads: ResponseCommentThreads) {
        if (responseCommentThreads.commentThreads.size > 0) {
            setPlayViewVisibility(ContentViewtype.RECYCLER_VIEW_COMMENT)

            mCurrentCommentPageToken = responseCommentThreads.nextPageToken
            if (mCurrentCommentPageToken == null || mCurrentCommentPageToken!!.isEmpty()) {
                mAdapterComment!!.setEndlessScroll(false)
                onScrollListener.setLoadMoreEnable(false)
            }
            mAdapterComment!!.addDataItems(responseCommentThreads.commentThreads)
        } else {
            setPlayViewVisibility(ContentViewtype.NO_COMMENT)
        }
    }

    private val mCommentListener = object : OnItemClickListener<CommentThread> {
        override fun onDataItemClick(dataItem: CommentThread?) {
            // Currently do nothing
        }

        override fun onErrorLoadMoreRetry() {
            mAdapterComment!!.setErrorLoadingMore(false)
            loadMoreComments()
        }

    }

    /**
     * Initialize activity's view on create
     */
    private fun setupViews() {
        //Setup youtube player view
        mYoutubePlayerFragment = KYoutubePlayerFragment.newInstance()
        fragmentManager.beginTransaction().replace(R.id.youtube_player, mYoutubePlayerFragment).commit()
        mYoutubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, onInitializedListener)

        btn_record!!.setOnClickListener({ onRecordButtonClick() })
        btn_recording!!.setOnClickListener({ onRecordButtonClick() })

        //Setup comment view
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerview_comments!!.layoutManager = layoutManager
        recyclerview_comments!!.addItemDecoration(InsetDividerDecoration(
                ViewHolderComment::class.java,
                resources.getDimensionPixelSize(R.dimen.divider_height),
                resources.getDimensionPixelSize(R.dimen.keyline_1),
                ContextCompat.getColor(this, R.color.divider_light)))
        mAdapterComment = KAdapterComments(this, mCommentListener)

        recyclerview_comments!!.adapter = mAdapterComment
        onScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemCount: Int) {
                loadMoreComments()
            }
        }
        recyclerview_comments!!.addOnScrollListener(onScrollListener)
    }

    private fun loadMoreComments() {
        val obsCommentMore = KApplication.rxYoutubeAPIService.getVideoCommentsNext(mCurrentVideoId!!, mCurrentCommentPageToken!!)
        compositeSubscriptionForOnStop.add(obsCommentMore.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<ResponseCommentThreads>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        mAdapterComment!!.setErrorLoadingMore(true)
                    }

                    override fun onNext(responseCommentThreads: ResponseCommentThreads) {
                        mCurrentCommentPageToken = responseCommentThreads.nextPageToken
                        if (mCurrentCommentPageToken == null || mCurrentCommentPageToken!!.isEmpty()) {
                            mAdapterComment!!.setEndlessScroll(false)
                            onScrollListener.setLoadMoreEnable(false)
                        }
                        mAdapterComment!!.addDataItems(responseCommentThreads.commentThreads)
                    }
                }))
    }

    /**
     * Build a dialog that appear when Stop recording button is clicked
     */
    private fun buildPostRecordDialog() {
        if (mYoutubePlayer != null && mYoutubePlayer!!.isPlaying) {
            Log.d(DEBUG_TAG, "Pause YoutubePlayer")
            mYoutubePlayer!!.pause()
        }


        var postRecordDialog: AlertDialog? = null
        val postRecDialgBuilder = AlertDialog.Builder(this)

        val inflater = this.layoutInflater
        val actionDialog = inflater.inflate(R.layout.dialog_post_recording, null)
        postRecDialgBuilder.setView(actionDialog)
        postRecDialgBuilder.setCancelable(false)

//        val recordResume = actionDialog.findViewById(R.id.action_record_resume) as RelativeLayout
        val recordRestart = actionDialog.findViewById(R.id.action_record_restart) as RelativeLayout
        val recordNewSong = actionDialog.findViewById(R.id.action_record_new_song) as RelativeLayout
        val recordSaveEarly = actionDialog.findViewById(R.id.action_record_save_early) as RelativeLayout

//        recordResume.setOnClickListener {
//            switchRecordButton(true)
//            mYoutubePlayer!!.play()
//            //Resume recording
//            mRecorder!!.start(mCurrentSavedFilename, true)
//            startRecordingTimer()
//            if (postRecordDialog != null) {
//                postRecordDialog!!.dismiss()
//            }
//        }

        recordRestart.setOnClickListener {
            switchRecordButton(true)
            //Reset youtube player
            mYoutubePlayer!!.seekToMillis(0)
            mYoutubePlayer!!.play()
            //Restart recording
            mRecorder!!.start(mCurrentSavedFilename, false)
            //Reset timer
            recorderSecondsElapsed = 0
            startRecordingTimer()
            if (postRecordDialog != null) {
                postRecordDialog!!.dismiss()
            }
        }

        recordNewSong.setOnClickListener {
            // Delete current recorded song
            Thread(Runnable {
                val recordFileDir = File(KApplication.RECORDING_DIRECTORY_URI)
                val recordedFile = File(recordFileDir, mCurrentSavedFilename!!)

                if (recordedFile.exists()) {
                    Log.d(DEBUG_TAG, "Discard recorded song: " + mCurrentSavedFilename)
                    recordedFile.delete()
                }
            }).run()

            val intent = Intent(this, KActivityHome::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            this@KActivityPlayVideo.finish()
            mRecorder!!.releaseRecorder()
            if (postRecordDialog != null) {
                postRecordDialog!!.dismiss()
            }
        }

        recordSaveEarly.setOnClickListener { view ->
            val intent = Intent(this, KActivityMyRecording::class.java)
            startActivity(intent)
            this@KActivityPlayVideo.finish()
            mRecorder!!.releaseRecorder()
            if (postRecordDialog != null) {
                postRecordDialog!!.dismiss()
            }
        }
        postRecordDialog = postRecDialgBuilder.create()
        postRecordDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.kactivity_play_video, menu)
        // Setup for favorite icon
        val favoriteMenu = menu.findItem(R.id.menu_favourite)

        val favoriteVideo = realm.where(FavoriteRealm::class.java).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, mCurrentVideoId).findFirst()

        if (favoriteVideo == null) {
            mFavoriteState = false
            favoriteMenu.setIcon(R.drawable.drawable_menu_favourite)
        } else {
            mFavoriteState = true
            favoriteMenu.setIcon(R.drawable.drawable_menu_favourite_added)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val menuId = item.itemId
        when (menuId) {
            R.id.menu_favourite -> handleFavoriteClick(item)
        }

        return true
    }
    /**
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
     */

    /**
     * Handle favorite click event
     * If this video is already in the favorite list, remove it.
     * Otherwise, add it to the current favorite video list and change the icon of Favorite button

     * @param favoriteMenuItem : menu item
     */
    private fun handleFavoriteClick(favoriteMenuItem: MenuItem) {
        val favoriteInserted = realm.where(FavoriteRealm::class.java).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, mCurrentVideoId).findFirst()

        if (favoriteInserted == null) {// Not in the favorite list -> add it
            realm.executeTransaction {
                val favoriteVideo = realm.createObject(FavoriteRealm::class.java, System.currentTimeMillis())
                favoriteVideo.video_id = mCurrentVideoId
                Toast.makeText(this@KActivityPlayVideo, "Added to the favorite list", Toast.LENGTH_SHORT).show()
                KApplication.eventBus.post(EventUpdateFavoriteList())
            }
            favoriteMenuItem.setIcon(R.drawable.drawable_menu_favourite_added)
            Toast.makeText(this, "Added to the favorite list", Toast.LENGTH_SHORT).show()
        } else {// Currently in favorite list -> remove it
            realm.executeTransaction {
                favoriteInserted.deleteFromRealm()
                Toast.makeText(this@KActivityPlayVideo, "Removed from the favorite list", Toast.LENGTH_SHORT).show()
                KApplication.eventBus.post(EventUpdateFavoriteList())
            }

            favoriteMenuItem.setIcon(R.drawable.drawable_menu_favourite)
            Toast.makeText(this, "Removed to the favorite list", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Event of running prepare recording countdown

     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventPrepareCountdownRunning(event: EventPrepareRecordingCountdown) {
        val current = event.currentValue
        Log.d(DEBUG_TAG, "Countdown: value = " + current)

        if (current == 0) {
            tv_countdown!!.text = "READY!!!"
        } else {
            tv_countdown!!.text = current.toString()
        }
    }

    /**
     * Start updating recording timer
     */
    private fun startRecordingTimer() {
        handler.post(updateTimer)
    }

    /**
     * Stop updating recording timer
     */
    private fun stopRecordingTimer() {
        handler.removeCallbacks(updateTimer)
    }

    /**
     * Start recording voice
     */
    private fun startRecording() {
        switchRecordButton(true)

        /*Auto configure audio output volume (try to prevent too loud beat)*/
        configAudioVolume()

        mYoutubePlayer!!.play()
        saved_filename!!.text = mCurrentSavedFilename
        mRecorder!!.start(mCurrentSavedFilename, false)
        startRecordingTimer()

        setActivityState(mRecordingState)
    }

    /**
     * Stop recording voice
     */
    fun stopRecording() {
        if (mRecorder != null && mRecorder!!.mIsRecording.get()) {
            switchRecordButton(false)
            stopRecordingTimer()
            mRecorder!!.stop()
            buildPostRecordDialog()
        }
    }

    /**
     * If user record directly, this lead to a problem that the beat volume is too loud and user's
     * voice volume is too low -> pre-configure audio volume, user may change later if they find it's
     * not suitable
     */
    private fun configAudioVolume() {
        // This value's result maybe vary between devices
        // Just a start number, user may change volume if it's too loud or too small
        val percent = 0.4f

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val autoVol = (maxVol * percent).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, autoVol, 0)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Logger.d("onSaveInstanceState")
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Logger.d("onRestoreInstanceState")
    }


    override fun onBackPressed() {
        Log.d(DEBUG_TAG, "onBackPressed")

        val favoriteVideo = realm.where(FavoriteRealm::class.java).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, mCurrentVideoId).findFirst()
        val changeState = if (favoriteVideo != null) true else false

        if (changeState != mFavoriteState) {
            // User change state of favorite
            Log.d(DEBUG_TAG, "Send intent to update favorite video list")
            val updateFavoriteList = Intent()
            setResult(Activity.RESULT_OK, updateFavoriteList)
        }
        super.onBackPressed()
    }

    //
    private fun setPlayViewVisibility(contentViewtype: ContentViewtype) {
        when (contentViewtype) {
            ContentViewtype.PROGRESSBAR -> {
                progressbar_comment!!.visibility = View.VISIBLE
                recyclerview_comments!!.visibility = View.GONE
                content_no_comment!!.visibility = View.GONE
                content_error_loading!!.visibility = View.GONE
            }
            ContentViewtype.RECYCLER_VIEW_COMMENT -> {
                progressbar_comment!!.visibility = View.GONE
                recyclerview_comments!!.visibility = View.VISIBLE
                content_no_comment!!.visibility = View.GONE
                content_error_loading!!.visibility = View.GONE
            }
            ContentViewtype.NO_COMMENT -> {
                progressbar_comment!!.visibility = View.GONE
                recyclerview_comments!!.visibility = View.GONE
                content_no_comment!!.visibility = View.VISIBLE
                content_error_loading!!.visibility = View.GONE
            }
            ContentViewtype.ERROR_LOAD_COMMENT -> {
                progressbar_comment!!.visibility = View.GONE
                recyclerview_comments!!.visibility = View.GONE
                content_no_comment!!.visibility = View.GONE
                content_error_loading!!.visibility = View.VISIBLE
            }
        }
    }

    private fun setLayoutVisibility(layoutType: LayoutType) {
        when (layoutType) {
            LayoutType.ERROR_NO_CONNECTION -> {
                layout_connection_error!!.visibility = View.VISIBLE
                layout_play_video!!.visibility = View.GONE
                layout_record!!.visibility = View.GONE
                youtube_player.visibility = View.GONE
            }
            LayoutType.PLAYING -> {
                layout_connection_error!!.visibility = View.GONE
                layout_play_video!!.visibility = View.VISIBLE
                layout_record!!.visibility = View.GONE
                youtube_player.visibility = View.VISIBLE
            }
            LayoutType.RECORDING -> {
                layout_ready!!.visibility = View.GONE
                layout_connection_error!!.visibility = View.GONE
                layout_play_video!!.visibility = View.GONE
                layout_record!!.visibility = View.VISIBLE
                youtube_player.visibility = View.VISIBLE
            }
            LayoutType.PREPARE_RECORDING -> {
                layout_ready!!.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Display a prepare screen before starting recording
     */
    private inner class PrepareRecordingTask : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            if (mYoutubePlayer != null) {
                Log.d(DEBUG_TAG, "PrepareRecordingTask: youtubePlayer != null")
                if (mYoutubePlayer!!.isPlaying) {
                    Log.d(DEBUG_TAG, "PrepareRecordingTask: pause youtube")
                    mYoutubePlayer!!.pause()
                }
                mYoutubePlayer!!.seekToMillis(0)
            }

            setLayoutVisibility(LayoutType.PREPARE_RECORDING)
        }

        override fun doInBackground(vararg voids: Void): Void? {
            for (i in 3 downTo 0) {
                KApplication.eventBus.post(EventPrepareRecordingCountdown(i))
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            setLayoutVisibility(LayoutType.RECORDING)

            // Start recording voice
            startRecording()
        }
    }

    /**
     * Task to delete existed recorded file before starting recording
     */
    private inner class DeleteRecordedFileTask : AsyncTask<Void, Void, Boolean>() {

        internal var deleteProgessDialog = ProgressDialog(this@KActivityPlayVideo)

        override fun onPreExecute() {
            super.onPreExecute()

            deleteProgessDialog.setMessage("Deleting old recorded file...")
            deleteProgessDialog.isIndeterminate = true
            deleteProgessDialog.show()
        }

        override fun doInBackground(vararg params: Void): Boolean? {
            val recordFileDir = File(KApplication.RECORDING_DIRECTORY_URI)
            val recordedFile = File(recordFileDir, mCurrentSavedFilename!!)

            return recordedFile.exists() && recordedFile.delete()
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

            if (result!!) {
                deleteProgessDialog.dismiss()
                PrepareRecordingTask().execute()
            } else {
                Toast.makeText(this@KActivityPlayVideo, resources.getString(R.string.toast_delete_file_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getUnitializeState(): KActivityPlayVideoBaseState {
        return this.mUninitState
    }

    fun getPlayingState(): KActivityPlayVideoBaseState {
        return this.mPlayingState
    }

    fun getRecordingState(): KActivityPlayVideoBaseState {
        return this.mRecordingState
    }

    fun getCurrentVideoId(): String {
        return this.mCurrentVideoId
    }

    fun reinitializeYoutubePlayerFragment(currentVideoPos: Int) {
        this.mLastVideoPos = currentVideoPos
        mYoutubePlayerFragment = KYoutubePlayerFragment.newInstance()
        fragmentManager.beginTransaction().replace(R.id.youtube_player, mYoutubePlayerFragment).commit()
        mYoutubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, onReInitializedListener)
        mYoutubePlayer!!.cueVideo(mCurrentVideoId, mLastVideoPos)
    }

    companion object {

        private val DEBUG_TAG = KActivityPlayVideo::class.java.simpleName
        private val RECOVERY_REQUEST = 1
    }

    enum class ContentViewtype {
        PROGRESSBAR,
        RECYCLER_VIEW_COMMENT,
        NO_COMMENT,
        ERROR_LOAD_COMMENT
    }

    enum class LayoutType {
        ERROR_NO_CONNECTION,
        PLAYING,
        RECORDING,
        PREPARE_RECORDING
    }
}
