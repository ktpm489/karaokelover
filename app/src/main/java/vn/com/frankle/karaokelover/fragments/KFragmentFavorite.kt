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
import kotlinx.android.synthetic.main.layout_fragment_my_favorite.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import vn.com.frankle.karaokelover.KActivityPlayVideo
import vn.com.frankle.karaokelover.KApplication
import vn.com.frankle.karaokelover.KSharedPreference
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.adapters.KAdapterYoutbeVideoSearchLimit
import vn.com.frankle.karaokelover.database.entities.Favorite
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import vn.com.frankle.karaokelover.database.tables.FavoriteTable
import vn.com.frankle.karaokelover.services.ReactiveHelper
import vn.com.frankle.karaokelover.util.Utils
import vn.com.frankle.karaokelover.views.SpaceItemDecoration
import java.util.*
import javax.inject.Inject

/**
 * Created by duclm on 9/18/2016.
 */

class KFragmentFavorite : Fragment() {

    @Inject lateinit var storIOSQLite: StorIOSQLite

    private val compositeSubscriptionForOnStop = CompositeSubscription()

//    @BindView(R.id.progressbar_favorite)
//    internal var mProgressBar: ProgressBar? = null
//    @BindView(R.id.recyclerview_my_favorite)
//    internal var mRecyclerView: RecyclerView? = null


    private var mContext: Context? = null
    private val mAppPrefs = KSharedPreference()
    private var mCurSizeList = 0
    private var mFavoriteAdapter: KAdapterYoutbeVideoSearchLimit? = null

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
    }

    override fun onStart() {
        super.onStart()
        Log.d(DEBUG_TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(DEBUG_TAG, "onStop")
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

        val application = activity.application as KApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(DEBUG_TAG, "onCreateView")

        val layout = inflater!!.inflate(R.layout.layout_fragment_my_favorite, container, false)

//        ButterKnife.bind(this, layout)

        return layout
    }

    private fun getFavoriteVideosFromDB() {
        val obsGetFavorites = storIOSQLite
                .get()
                .listOfObjects(Favorite::class.java)
                .withQuery(FavoriteTable.QUERY_ALL_DISTINCT)
                .prepare()
                .asRxObservable() // Get Result as rx.Observable and subscribe to further updates of tables from Query!
                .concatMap {
                    Observable.from(it).map { it.video_id }.toList()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // All Rx operations work on Schedulers.io()
        compositeSubscriptionForOnStop.add(obsGetFavorites.subscribe({
            loadFavoriteVideos(it)
        }))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(DEBUG_TAG, "onActivityResult")
        when (requestCode) {
            KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST -> reloadIfNecessary(data)
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

            mFavoriteAdapter!!.addDataItemList(favoriteVideos)
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
     * Reload the favorite video list if necessary (change from other activity)

     * @param resultData : intent data sent by called activity
     */
    fun reloadIfNecessary(resultData: Intent?) {
        if (resultData != null) {
            val videoId = resultData.getStringExtra("video_id")
            if (mAppPrefs.isInFavoriteList(mContext, videoId)) {
                // User has add this video to the favorite list
                val newVideo = ArrayList<String>()
                newVideo.add(videoId)
                loadFavoriteVideos(newVideo)
            } else {
                // User remove this video from the favorite list
                mFavoriteAdapter!!.removeVideoFromList(videoId)
            }
        }
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

    companion object {

        val TAG = "FRAGMENT_FAVORITE"
        @JvmField val REQUEST_CODE_RELOAD_FAVORITE_LIST = 111
        private val DEBUG_TAG = KFragmentFavorite::class.java.simpleName
    }
}
