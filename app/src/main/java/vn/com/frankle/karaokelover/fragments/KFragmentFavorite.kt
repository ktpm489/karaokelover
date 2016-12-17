package vn.com.frankle.karaokelover.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import io.realm.Realm
import kotlinx.android.synthetic.main.layout_fragment_my_favorite.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import vn.com.frankle.karaokelover.KApplication
import vn.com.frankle.karaokelover.KSharedPreference
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.activities.KActivityPlayVideo
import vn.com.frankle.karaokelover.adapters.KAdapterYoutbeVideoSearchLimit
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm
import vn.com.frankle.karaokelover.events.EventUpdateFavoriteList
import vn.com.frankle.karaokelover.services.ReactiveHelper
import vn.com.frankle.karaokelover.util.Utils
import vn.com.frankle.karaokelover.views.SpaceItemDecoration
import javax.inject.Inject

/**
 * Created by duclm on 9/18/2016.
 */

class KFragmentFavorite : Fragment() {

    @Inject lateinit var storIOSQLite: StorIOSQLite
    @Inject lateinit var realm: Realm

    private val compositeSubscriptionForOnStop = CompositeSubscription()


    private var mContext: Context? = null
    private val mAppPrefs = KSharedPreference()
    private var mCurSizeList = 0
    private var mFavoriteAdapter: KAdapterYoutbeVideoSearchLimit? = null
    // Flag to indicate whether reload the video list or not
    // Basically when realm results get notified by change somewhere, we set this flag to true
    // and we will use this flag at onResume
    private var mRefreshList = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onPause() {
        super.onPause()
        Log.d(DEBUG_TAG, "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d(DEBUG_TAG, "onResume")
        if (this.mRefreshList) {
            // This flag is true -> need to reload list of favorite videos
            getFavoriteVideosFromDB()
            // Reset flag
            this.mRefreshList = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(DEBUG_TAG, "onStart")
        KApplication.eventBus.register(this)
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Log.d(DEBUG_TAG, "onSaveInstanceState")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(DEBUG_TAG, "onViewStateRestored")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(DEBUG_TAG, "onActivityCreated")
        setupFavoriteView()
        // Load list of favorite video
        getFavoriteVideosFromDB()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retain this fragment across configuration changes.
        retainInstance = true

        val application = activity.application as KApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(DEBUG_TAG, "onCreateView")

        val layout = inflater!!.inflate(R.layout.layout_fragment_my_favorite, container, false)

        return layout
    }

    /**
     * Read list of favorite videos from Realm database
     */
    fun getFavoriteVideosFromDB() {
        val favorites = realm.where(FavoriteRealm::class.java).findAll()

        if (favorites.isEmpty()) (
                Toast.makeText(mContext, "There is no video in favorite list", Toast.LENGTH_SHORT).show()
                ) else {
            val favoriteVideoIds = favorites.map { it.video_id!! }
            loadFavoriteVideos(favoriteVideoIds)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(DEBUG_TAG, "onActivityResult")
        when (requestCode) {
            KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST -> getFavoriteVideosFromDB()
        }

    }

    /**
     * Setup view for displaying search result
     */
    private fun setupFavoriteView() {
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerview_my_favorite.layoutManager = layoutManager
        recyclerview_my_favorite.setHasFixedSize(true)
        recyclerview_my_favorite.addItemDecoration(SpaceItemDecoration(Utils.convertDpToPixel(mContext, 0), SpaceItemDecoration.VERTICAL))
        mFavoriteAdapter = KAdapterYoutbeVideoSearchLimit(mContext)
        val clickListenerObservable = mFavoriteAdapter!!.itemClickListener
        compositeSubscriptionForOnStop.add(clickListenerObservable.subscribe({ handleOnVideoClickListener(it) }))
        recyclerview_my_favorite.adapter = mFavoriteAdapter
    }

    private fun loadFavoriteVideos(listFavoriteId: List<String>) {
        setLoadingState(true)
        mCurSizeList = listFavoriteId.size

        val favoriteRequest = ReactiveHelper.getFavoritesVideos(listFavoriteId)
        compositeSubscriptionForOnStop.add(favoriteRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.handleFavoriteVideoList(it) }))
    }

    private fun handleFavoriteVideoList(favoriteVideos: List<VideoSearchItem>) {
        if (favoriteVideos.isNotEmpty()) {
            setLoadingState(false)

            mFavoriteAdapter!!.setDataItemList(favoriteVideos)
        } else {
            Toast.makeText(mContext, "Empty favorite videos list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleOnVideoClickListener(video: VideoSearchItem) {
        val playVideoItent = Intent(mContext, KActivityPlayVideo::class.java)
        playVideoItent.putExtra("title", video.title)
        playVideoItent.putExtra("videoid", video.videoId)
        playVideoItent.putExtra("from_favorite", true)
        startActivityForResult(playVideoItent, REQUEST_CODE_RELOAD_FAVORITE_LIST)
    }

    /**
     * Switch visiblity of ProgressBar and RecyclerView

     * @param loading : true if display progressbar
     */
    private fun setLoadingState(loading: Boolean) {
        if (loading) {
            progressbar_favorite.visibility = View.VISIBLE
            recyclerview_my_favorite.visibility = View.GONE
        } else {
            progressbar_favorite.visibility = View.GONE
            recyclerview_my_favorite.visibility = View.VISIBLE
        }
    }

    /**
     * Handle event: update favorite video list
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun OnEventUpdateFavoriteList(event: EventUpdateFavoriteList) {
        Log.d(DEBUG_TAG, "Event: update favorite list")
        this.mRefreshList = true
    }

    companion object {

        @JvmField val TAG = KFragmentFavorite::class.java.simpleName
        @JvmField val REQUEST_CODE_RELOAD_FAVORITE_LIST = 111
        private val DEBUG_TAG = KFragmentFavorite::class.java.simpleName

    }
}
