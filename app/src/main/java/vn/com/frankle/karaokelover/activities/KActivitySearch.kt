package vn.com.frankle.karaokelover.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import butterknife.OnClick
import io.realm.Realm
import kotlinx.android.synthetic.main.content_error_loading.*
import kotlinx.android.synthetic.main.layout_activity_search.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import vn.com.frankle.karaokelover.KApplication
import vn.com.frankle.karaokelover.KSharedPreference
import vn.com.frankle.karaokelover.R
import vn.com.frankle.karaokelover.adapters.EndlessRecyclerViewScrollListener
import vn.com.frankle.karaokelover.adapters.KAdapterYoutubeVideoSearch
import vn.com.frankle.karaokelover.adapters.RecyclerViewEndlessScrollBaseAdapter
import vn.com.frankle.karaokelover.database.entities.VideoSearchItem
import vn.com.frankle.karaokelover.database.realm.FavoriteRealm
import vn.com.frankle.karaokelover.events.EventPopupMenuItemClick
import vn.com.frankle.karaokelover.services.ReactiveHelper
import vn.com.frankle.karaokelover.util.AnimUtils
import vn.com.frankle.karaokelover.util.ImeUtils
import vn.com.frankle.karaokelover.util.Utils
import vn.com.frankle.karaokelover.util.ViewUtils
import vn.com.frankle.karaokelover.views.SpaceItemDecoration
import vn.com.frankle.karaokelover.views.widgets.BaselineGridTextView
import java.util.*
import javax.inject.Inject


/**
 * Created by duclm on 7/31/2016.
 */

class KActivitySearch : AppCompatActivity() {

    @Inject lateinit var realm: Realm

    private val compositeSubscriptionForOnStop = CompositeSubscription()


    private var noResults: BaselineGridTextView? = null
    private var auto: Transition? = null

    private var searchBackDistanceX: Int = 0
    private var searchIconCenterX: Int = 0
    private var dismissing: Boolean = false
    private var mCurrentSearchQuery: String? = null
    private var mNextPageToken: String? = null
    private val mSharedPref = KSharedPreference(this@KActivitySearch)
    // This set is used to store video id that change its favorite state
    // When the set is empty, it means that the favorite video list remains unchanged
    private var mFavoriteStateSet: HashSet<String> = hashSetOf()

    private var mSearchAdapter: KAdapterYoutubeVideoSearch? = null

    private val mOnItemClickListener = object : RecyclerViewEndlessScrollBaseAdapter.OnItemClickListener<VideoSearchItem> {
        override fun onDataItemClick(dataItem: VideoSearchItem) {
            handleOnVideoClickListener(dataItem)
        }

        override fun onErrorLoadMoreRetry() {
            mSearchAdapter!!.setErrorLoadingMore(false)
            searchMoreVideo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as KApplication
        application.appComponent.inject(this)

        // init realm instance
        realm = Realm.getDefaultInstance()

        setContentView(R.layout.layout_activity_search)
        setupSearchView()
        setupSearchResultView()
        auto = TransitionInflater.from(this).inflateTransition(R.transition.auto)


        // extract the search icon's location passed from the launching activity, minus 4dp to
        // compensate for different paddings in the views
        searchBackDistanceX = intent.getIntExtra(EXTRA_MENU_LEFT, 0) - TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        searchIconCenterX = intent.getIntExtra(EXTRA_MENU_CENTER_X, 0)

        // translate icon to match the launching screen then animate back into position
        searchback_container.translationX = searchBackDistanceX.toFloat()
        searchback_container.animate()
                .translationX(0f)
                .setDuration(650L).interpolator = AnimUtils.getFastOutSlowInInterpolator(this)
        // transform from search icon to back icon
        val searchToBack = ContextCompat
                .getDrawable(this, R.drawable.avd_search_to_back) as AnimatedVectorDrawable
        searchback.setImageDrawable(searchToBack)
        searchToBack.start()
        // for some reason the animation doesn't always finish (leaving a part arrow!?) so after
        // the animation set a static drawable. Also animation callbacks weren't added until API23
        // so using post delayed :(
        // TODO fix properly!!
        searchback!!.postDelayed({
            searchback!!.setImageDrawable(ContextCompat.getDrawable(this@KActivitySearch,
                    R.drawable.ic_arrow_back_padded))
        }, 600L)

        // fade in the other search chrome
        search_background.animate()
                .alpha(1f)
                .setDuration(300L).interpolator = AnimUtils.getLinearOutSlowInInterpolator(this)
        search_view.animate()
                .alpha(1f)
                .setStartDelay(400L)
                .setDuration(400L)
                .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(this))
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        search_view.requestFocus()
                        ImeUtils.showIme(search_view)
                    }
                })

        // animate in a scrim over the content behind
        scrim!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                scrim!!.viewTreeObserver.removeOnPreDrawListener(this)
                val showScrim = AnimatorSet()
                showScrim.playTogether(
                        ViewAnimationUtils.createCircularReveal(
                                scrim,
                                searchIconCenterX,
                                search_background.bottom,
                                0f,
                                Math.hypot(searchBackDistanceX.toDouble(), (scrim!!.height - search_background.bottom).toDouble()).toFloat()),
                        ObjectAnimator.ofArgb(
                                scrim,
                                ViewUtils.BACKGROUND_COLOR,
                                Color.TRANSPARENT,
                                ContextCompat.getColor(this@KActivitySearch, R.color.scrim)))
                showScrim.duration = 400L
                showScrim.interpolator = AnimUtils.getLinearOutSlowInInterpolator(this@KActivitySearch)
                showScrim.start()
                return false
            }
        })
        onNewIntent(intent)
    }

    /**
     * Video click handler

     * @param video : video is clicked
     */
    private fun handleOnVideoClickListener(video: VideoSearchItem) {
        val playVideoItent = Intent(this, KActivityPlayVideo::class.java)
        playVideoItent.putExtra("title", video.title)
        playVideoItent.putExtra("videoid", video.videoId)
        startActivity(playVideoItent)
    }

    /**
     * Setup view for displaying search result
     */
    private fun setupSearchResultView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        search_results.layoutManager = layoutManager
        search_results.setHasFixedSize(true)
        search_results.addItemDecoration(SpaceItemDecoration(Utils.convertDpToPixel(this, 0), SpaceItemDecoration.VERTICAL))
        mSearchAdapter = KAdapterYoutubeVideoSearch(this, mOnItemClickListener)
        search_results.adapter = mSearchAdapter
        search_results.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemCount: Int) {
                searchMoreVideo()
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            mCurrentSearchQuery = intent.getStringExtra(SearchManager.QUERY)
            if (!TextUtils.isEmpty(mCurrentSearchQuery)) {
                search_view.setQuery(mCurrentSearchQuery, false)
                searchYoutubeVideo()
                // Store search query for suggestion later
//                val suggestions: SearchRecentSuggestions = SearchRecentSuggestions(this@KActivitySearch,
//                        KaraokeSearchProvider.AUTHORITY, KaraokeSearchProvider.MODE)
//                suggestions.saveRecentQuery(mCurrentSearchQuery, null)
            }
        }
    }

    override fun onBackPressed() {
        dismiss()
        super.onBackPressed()
    }

    override fun onPause() {
        Log.d(DEBUG_TAG, "OnPause")

        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0)
        // Check if favorite state list is not empty (it means that favorite videos list was changed)
        if (!mFavoriteStateSet.isEmpty()) {
            mSharedPref.setFavoriteListReloadFlag(this@KActivitySearch, true)
            mFavoriteStateSet.clear()
        }
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        KApplication.eventBus.register(this)
    }

    override fun onStop() {
        KApplication.eventBus.unregister(this)
        super.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()

        //Close Realm object
        realm.close()

        compositeSubscriptionForOnStop.unsubscribe()
    }

    @OnClick(R.id.container_content_error)
    fun retryOnConnectionError() {
        searchYoutubeVideo()
    }

    @OnClick(R.id.scrim, R.id.searchback)
    fun dismiss() {
        if (dismissing) return
        dismissing = true

        // translate the icon to match position in the launching activity
        searchback_container.animate()
                .translationX(searchBackDistanceX.toFloat())
                .setDuration(600L)
                .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this))
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        finishAfterTransition()
                    }
                })
                .start()
        // transform from back icon to search icon
        val backToSearch = ContextCompat
                .getDrawable(this, R.drawable.avd_back_to_search) as AnimatedVectorDrawable
        searchback!!.setImageDrawable(backToSearch)
        // clear the background else the touch ripple moves with the translation which looks bad
        searchback!!.background = null
        backToSearch.start()
        // fade out the other search chrome
        search_view.animate()
                .alpha(0f)
                .setStartDelay(0L)
                .setDuration(120L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // prevent clicks while other anims are finishing
                        search_view!!.visibility = View.INVISIBLE
                    }
                })
                .start()
        search_background!!.animate()
                .alpha(0f)
                .setStartDelay(300L)
                .setDuration(160L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(null)
                .start()
        if (search_toolbar!!.z != 0f) {
            search_toolbar!!.animate()
                    .z(0f)
                    .setDuration(600L)
                    .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                    .start()
        }

        // if we're showing search results, circular hide them
        if (results_container!!.height > 0) {
            val closeResults = ViewAnimationUtils.createCircularReveal(
                    results_container,
                    searchIconCenterX,
                    0,
                    Math.hypot(searchIconCenterX.toDouble(), results_container!!.height.toDouble()).toFloat(),
                    0f)
            closeResults.duration = 500L
            closeResults.interpolator = AnimUtils.getFastOutSlowInInterpolator(this@KActivitySearch)
            closeResults.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    results_container!!.visibility = View.INVISIBLE
                }
            })
            closeResults.start()
        }

        // if error container is being showed, circular hide them
        if (container_content_error.height > 0) {
            val closeErrorLayout = ViewAnimationUtils.createCircularReveal(
                    container_content_error,
                    searchIconCenterX,
                    0,
                    Math.hypot(searchIconCenterX.toDouble(), container_content_error!!.height.toDouble()).toFloat(),
                    0f)
            closeErrorLayout.duration = 500L
            closeErrorLayout.interpolator = AnimUtils.getFastOutSlowInInterpolator(this@KActivitySearch)
            closeErrorLayout.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    container_content_error!!.visibility = View.GONE
                }
            })
            closeErrorLayout.start()
        }

        // fade out the scrim
        scrim!!.animate()
                .alpha(0f)
                .setDuration(400L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this))
                .setListener(null)
                .start()
    }


    private fun setupSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search_view!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        // hint, inputType & ime options seem to be ignored from XML! Set in code
        search_view!!.queryHint = getString(R.string.search_hint)
        search_view!!.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        search_view!!.imeOptions = search_view!!.imeOptions or EditorInfo.IME_ACTION_SEARCH or
                EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        search_view!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mCurrentSearchQuery = query
                searchYoutubeVideo()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (TextUtils.isEmpty(query)) {
                    clearContainerContent()
                }
                return true
            }
        })
        search_view!!.setOnQueryTextFocusChangeListener { v, hasFocus -> }
        scrim.setOnClickListener { dismiss() }
        searchback.setOnClickListener { dismiss() }
        content_error_loading.setOnClickListener {
            searchYoutubeVideo()
        }
    }

    /**
     * Clear view container's content when hitting clear search text button
     */
    private fun clearContainerContent() {
        TransitionManager.beginDelayedTransition(container, auto)
        mSearchAdapter!!.removeAllDataItem()
        search_results!!.visibility = View.GONE
        progressbar!!.visibility = View.GONE
        results_scrim!!.visibility = View.GONE
        container_content_error!!.visibility = View.GONE
        setNoResultsVisibility(View.GONE)
    }

    private fun setNoResultsVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (findViewById(R.id.stub_no_search_results) as ViewStub).inflate() as BaselineGridTextView
                noResults!!.setOnClickListener { v ->
                    search_view!!.setQuery("", false)
                    search_view!!.requestFocus()
                    ImeUtils.showIme(search_view!!)
                }
            }
            val message = String.format(getString(R.string.no_search_results), search_view!!.query.toString())
            val ssb = SpannableStringBuilder(message)
            ssb.setSpan(StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            noResults!!.text = ssb
        }
        if (noResults != null) {
            noResults!!.visibility = visibility
        }
    }

    private fun handleSearchResult(searchResults: List<VideoSearchItem>) {
        Log.i("SearchResult", "Search result size = " + searchResults.size)

        if (searchResults.isNotEmpty()) {
            if (search_results!!.visibility != View.VISIBLE) {
                TransitionManager.beginDelayedTransition(container, auto)
                progressbar!!.visibility = View.GONE
                search_results!!.visibility = View.VISIBLE

                mSearchAdapter!!.setDataItems(searchResults)
            }
        } else {
            // No result to display
            TransitionManager.beginDelayedTransition(container, auto)
            progressbar!!.visibility = View.GONE
            setNoResultsVisibility(View.VISIBLE)
        }
    }

    /**
     * Handle result of loading more videos

     * @param loadMoreResult : the loaded more result
     */
    private fun handleResultLoadMore(loadMoreResult: List<VideoSearchItem>) {
        if (loadMoreResult.isNotEmpty()) {
            mSearchAdapter!!.addDataItems(loadMoreResult)
        }
    }

    private fun searchYoutubeVideo() {
        clearContainerContent()
        ImeUtils.hideIme(search_view!!)
        search_view!!.clearFocus()

        if (Utils.isOnline(this)) {
            switchConnectionErrorLayout(false)

            val karaokeQuery = mCurrentSearchQuery!! + " karaoke"

            val searchRequest: Observable<List<VideoSearchItem>> = KApplication.rxYoutubeAPIService
                    .searchKaraokeVideos(karaokeQuery)
                    .flatMap {
                        mNextPageToken = it.nextPageToken
                        Observable.from(it.items)
                                .subscribeOn(Schedulers.newThread())
                                .flatMap { ReactiveHelper.getStatisticsContentDetails(it) }
                    }
                    .toList()

            compositeSubscriptionForOnStop.add(searchRequest
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<List<VideoSearchItem>>() {
                        override fun onCompleted() {

                        }

                        override fun onError(e: Throwable) {
                            Log.e(DEBUG_TAG, "Error: " + e.message)
                            Log.e(DEBUG_TAG, "Error cause: " + e.stackTrace.toString())
                            switchConnectionErrorLayout(true)
                        }

                        override fun onNext(videoSearchItems: List<VideoSearchItem>) {
                            handleSearchResult(videoSearchItems)
                        }
                    }))


        } else {
            switchConnectionErrorLayout(true)
        }
    }

    /**
     * Load more search result
     */
    private fun searchMoreVideo() {
        Log.d(DEBUG_TAG, "Load more videos: mCurrentToken = " + mNextPageToken!!)
        val karaokeQuery = mCurrentSearchQuery!! + " karaoke"

        val loadMoreRequest: Observable<List<VideoSearchItem>> = KApplication.rxYoutubeAPIService
                .searchYoutubeVideoNext(karaokeQuery, mNextPageToken!!)
                .flatMap {
                    mNextPageToken = it.nextPageToken
                    Observable.from(it.items)
                            .subscribeOn(Schedulers.newThread())
                            .flatMap { ReactiveHelper.getStatisticsContentDetails(it) }
                }
                .toList()

        compositeSubscriptionForOnStop.add(loadMoreRequest
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<VideoSearchItem>>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        mSearchAdapter!!.setErrorLoadingMore(true)
                    }

                    override fun onNext(videoSearchItems: List<VideoSearchItem>) {
                        handleResultLoadMore(videoSearchItems)
                    }
                }))
    }

    private fun switchConnectionErrorLayout(isError: Boolean) {
        TransitionManager.beginDelayedTransition(container, auto)
        if (isError) {
            progressbar!!.visibility = View.GONE
            container_content_error!!.visibility = View.VISIBLE
            content_error_loading!!.visibility = View.VISIBLE
        } else {
            content_error_loading!!.visibility = View.GONE
            progressbar!!.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPopupMenuItemClick(event: EventPopupMenuItemClick) {
        val inserted = realm.where(FavoriteRealm::class.java).equalTo(FavoriteRealm.COLUMN_VIDEO_ID, event.data.videoId).findFirst()

        when (event.action) {
            EventPopupMenuItemClick.ACTION.ADD_FAVORITE -> {
                // All writes must be wrapped in a transaction to facilitate safe multi threading
                if (inserted == null) {
                    realm.executeTransaction {
                        val favoriteVideo = realm.createObject(FavoriteRealm::class.java, System.currentTimeMillis())
                        favoriteVideo.video_id = event.data.videoId
                        Toast.makeText(this, KApplication.appResource.getString(R.string.toast_added_favorite), Toast.LENGTH_SHORT).show()
                    }
                    if (!mFavoriteStateSet.remove(event.data.videoId!!)) {
                        mFavoriteStateSet.add(event.data.videoId!!)
                    }
                }
            }
            EventPopupMenuItemClick.ACTION.REMOVE_FAVORITE -> {
                realm.executeTransaction {
                    inserted.deleteFromRealm()
                    Toast.makeText(this, KApplication.appResource.getString(R.string.toast_removed_favorite), Toast.LENGTH_SHORT).show()
                }
                if (!mFavoriteStateSet.remove(event.data.videoId!!)) {
                    mFavoriteStateSet.add(event.data.videoId!!)
                }
            }
        }
    }

    companion object {
        val DEBUG_TAG = KActivitySearch::class.java.simpleName

        val EXTRA_MENU_LEFT = "EXTRA_MENU_LEFT"
        val EXTRA_MENU_CENTER_X = "EXTRA_MENU_CENTER_X"
        val EXTRA_QUERY = "EXTRA_QUERY"
        val RESULT_CODE_SAVE = 7

        fun createStartIntent(context: Context, menuIconLeft: Int, menuIconCenterX: Int): Intent {
            val starter = Intent(context, KActivitySearch::class.java)
            starter.putExtra(EXTRA_MENU_LEFT, menuIconLeft)
            starter.putExtra(EXTRA_MENU_CENTER_X, menuIconCenterX)
            return starter
        }
    }
}
