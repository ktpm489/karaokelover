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
import io.realm.Sort
import kotlinx.android.synthetic.main.content_connection_error.*
import kotlinx.android.synthetic.main.fragment_favorite_no_item.*
import kotlinx.android.synthetic.main.layout_fragment_my_favorite.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import vn.com.frankle.karaokelover.KApplication
import vn.com.frankle.karaokelover.KSharedPreference
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.activities.KActivityHome
import vn.com.frankle.karaokelover.activities.KActivityPlayVideo
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener
import vn.com.frankle.karaokelover.adapters.KAdapterYoutubeVideoSearch
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm
import vn.com.frankle.karaokelover.events.EventPopupMenuItemClick
import vn.com.frankle.karaokelover.services.ReactiveHelper
import vn.com.frankle.karaokelover.util.Utils
import vn.com.frankle.karaokelover.views.SpaceItemDecoration
import javax.inject.Inject

/**
 * Created by duclm on 9/18/2016.
 */

class KFragmentFavorite : Fragment() {

    enum class LayoutType {
        LOADING,
        NO_ITEM,
        LIST_ITEM,
        ERROR
    }

    @Inject lateinit var storIOSQLite: StorIOSQLite
    @Inject lateinit var realm: Realm

    private val compositeSubscriptionForOnStop = CompositeSubscription()


    private var mContext: Context? = null
    private val mAppPrefs = KSharedPreference(mContext)
    private var mCurSizeList = 0
    private lateinit var mFavoriteAdapter: KAdapterYoutubeVideoSearch
    internal lateinit var onScrollListener: EndlessRecyclerViewScrollListener
    private lateinit var mFullListVideoId: List<String>
    // Page index: for indicating current page in case of more than 15 videos in favorite list
    private var mCurrentPageIndex = 0
    // Flag to indicate whether reload the video list or not
    // Read this flag value from SharedPreference
    private var mIsNeededRealod = false
    // Flag is used to decide whether to call a function at onResume event or not
    // When the fragment first created, this flag is false that's mean it will not
    // call dedicated function at onResume
    private var mCallOnResume = false


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
        if (mCallOnResume) {
            mIsNeededRealod = mAppPrefs.getFavoriteListReloadFlag(mContext)
            if (mIsNeededRealod) {
                Log.d(DEBUG_TAG, "Reload favorite list from DB")
                getFavoriteVideosFromDB()
            }
        } else {
            mCallOnResume = true
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

    private val mOnItemClickListener = object : RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem> {
        override fun onDataItemClick(dataItem: VideoSearchItem) {
            handleOnVideoClickListener(dataItem)
        }

        override fun onErrorLoadMoreRetry() {
            mFavoriteAdapter.setErrorLoadingMore(false)
            loadMoreFavoriteVideos()
        }
    }

    fun setContentLayoutType(layoutType: LayoutType) {
        when (layoutType) {
            LayoutType.LOADING -> {
                progressbar_favorite.visibility = View.VISIBLE
                recyclerview_my_favorite.visibility = View.GONE
                layout_favorite_no_item.visibility = View.GONE
                layout_connection_error.visibility = View.GONE
            }

            LayoutType.NO_ITEM -> {
                progressbar_favorite.visibility = View.GONE
                recyclerview_my_favorite.visibility = View.GONE
                layout_favorite_no_item.visibility = View.VISIBLE
                layout_connection_error.visibility = View.GONE
            }

            LayoutType.LIST_ITEM -> {
                progressbar_favorite.visibility = View.GONE
                recyclerview_my_favorite.visibility = View.VISIBLE
                layout_favorite_no_item.visibility = View.GONE
                layout_connection_error.visibility = View.GONE
            }

            LayoutType.ERROR -> {
                progressbar_favorite.visibility = View.GONE
                recyclerview_my_favorite.visibility = View.GONE
                layout_favorite_no_item.visibility = View.GONE
                layout_connection_error.visibility = View.VISIBLE
            }
        }
    }


    /**
     * Read list of favorite videos from Realm database
     */
    fun getFavoriteVideosFromDB() {
        Log.d(DEBUG_TAG, "Get favorite list from DB")
        // Reset reload flag to false
        mAppPrefs.setFavoriteListReloadFlag(mContext, false)
        mCurrentPageIndex = 0

        val favorites = realm.where(FavoriteRealm::class.java).findAllSorted("id", Sort.DESCENDING)

        if (favorites.isEmpty()) (
                setContentLayoutType(LayoutType.NO_ITEM)
                ) else {
            mFullListVideoId = favorites.map { it.video_id!! }
            if (mFullListVideoId.size > 10) {
                mFavoriteAdapter.setEndlessScroll(true)
                onScrollListener.setLoadMoreEnable(true)
                loadFavoriteVideos(mFullListVideoId.subList(0, 9))
                mCurrentPageIndex += 1
            } else {
                loadFavoriteVideos(mFullListVideoId)
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(DEBUG_TAG, "onActivityResult")
//        when (requestCode) {
//            KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST -> getFavoriteVideosFromDB()
//        }
//
//    }

    /**
     * Setup view for displaying search result
     */
    private fun setupFavoriteView() {
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerview_my_favorite.layoutManager = layoutManager
        recyclerview_my_favorite.setHasFixedSize(true)
        recyclerview_my_favorite.addItemDecoration(SpaceItemDecoration(Utils.convertDpToPixel(mContext, 0), SpaceItemDecoration.VERTICAL))
        mFavoriteAdapter = KAdapterYoutubeVideoSearch(mContext!!, mOnItemClickListener)
        mFavoriteAdapter.setEndlessScroll(false)
        recyclerview_my_favorite.adapter = mFavoriteAdapter
        onScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemCount: Int) {
                loadMoreFavoriteVideos()
            }
        }
        onScrollListener.setLoadMoreEnable(false)
        recyclerview_my_favorite.addOnScrollListener(onScrollListener)
        layout_connection_error.setOnClickListener { getFavoriteVideosFromDB() }

        btn_explore.setOnClickListener {
            (activity as KActivityHome).backToHomeFragment()
        }
    }

    private fun loadFavoriteVideos(listFavoriteId: List<String>) {
        setContentLayoutType(LayoutType.LOADING)
        mCurSizeList = listFavoriteId.size

        val favoriteRequest = ReactiveHelper.getFavoritesVideos(listFavoriteId)
        compositeSubscriptionForOnStop.add(favoriteRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<VideoSearchItem>>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        setContentLayoutType(LayoutType.ERROR)
                    }

                    override fun onNext(t: List<VideoSearchItem>?) {
                        handleFavoriteVideoList(t)
                    }

                }))
    }

    private fun loadMoreFavoriteVideos() {
        val startIndex = mCurrentPageIndex * 10
        val calculatedIndex = mCurrentPageIndex * 10 + 9
        val endIndex: Int
        if (calculatedIndex < mFullListVideoId.size - 1) {
            endIndex = calculatedIndex
        } else {
            endIndex = mFullListVideoId.size - 1
        }
        val nextVideosId = mFullListVideoId.subList(startIndex, endIndex)

        val favoriteRequest = ReactiveHelper.getFavoritesVideos(nextVideosId)
        compositeSubscriptionForOnStop.add(favoriteRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<VideoSearchItem>>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        mFavoriteAdapter.setErrorLoadingMore(true)
                    }

                    override fun onNext(t: List<VideoSearchItem>?) {
                        mFavoriteAdapter.addDataItems(t)
                        if (endIndex == mFullListVideoId.size - 1) {
                            mFavoriteAdapter.setEndlessScroll(false)
                            onScrollListener.setLoadMoreEnable(false)
                        } else {
                            // Increase page index
                            mCurrentPageIndex += 1
                        }
                    }

                }))
    }

    private fun handleFavoriteVideoList(favoriteVideos: List<VideoSearchItem>?) {
        if (favoriteVideos!!.isNotEmpty()) {
            setContentLayoutType(LayoutType.LIST_ITEM)

            mFavoriteAdapter.setDataItems(favoriteVideos)
        } else {
            setContentLayoutType(LayoutType.NO_ITEM)
        }
    }

    private fun handleOnVideoClickListener(video: VideoSearchItem) {
        val playVideoItent = Intent(mContext, KActivityPlayVideo::class.java)
        playVideoItent.putExtra("title", video.title)
        playVideoItent.putExtra("videoid", video.videoId)
        playVideoItent.putExtra("from_favorite", true)
        startActivity(playVideoItent)
    }

    /**
     * Handle event: update favorite video list
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun OnPopUpMenuItemClick(event: EventPopupMenuItemClick) {
        Log.d(DEBUG_TAG, "Event: update favorite list")
        val inserted = realm.where(FavoriteRealm::class.java).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, event.data.videoId).findFirst()

        when (event.action) {
            EventPopupMenuItemClick.ACTION.REMOVE_FAVORITE -> {
                realm.executeTransaction {
                    inserted.deleteFromRealm()
                    Toast.makeText(mContext, KApplication.appResource.getString(R.string.toast_removed_favorite), Toast.LENGTH_SHORT).show()
                }
                mFavoriteAdapter.removeDataItem(event.data)
                if (mFavoriteAdapter.itemCount == 0) {
                    setContentLayoutType(LayoutType.NO_ITEM)
                } else if (mFavoriteAdapter.itemCount < 10) {
                    mFavoriteAdapter.setEndlessScroll(false)
                    onScrollListener.setLoadMoreEnable(false)
                }
            }
        }
    }

    companion object {

        @JvmField val TAG = KFragmentFavorite::class.java.simpleName
        private val DEBUG_TAG = KFragmentFavorite::class.java.simpleName

    }
}
