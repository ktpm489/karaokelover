package vn.com.frankle.karaokelover.activities;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.fragments.KFragmentArtists;
import vn.com.frankle.karaokelover.fragments.KFragmentFavorite;
import vn.com.frankle.karaokelover.fragments.KFragmentHome;
import vn.com.frankle.karaokelover.util.Utils;

public class KActivityHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String KEY_MENU_SELECTED_ITEM = "key_menu_selected_item";


    private static final int PERMISSION_AUDIO_RECORD = 0;
    private static final int RC_SEARCH = 0;
    private static final int FRAGMENT_HOME = 0;
    private static final int FRAGMENT_ARTIST = 1;
    private static final int FRAGMENT_FAVORITE = 2;
    private final String DEBUG_TAG = this.getClass().getSimpleName();
    @BindView(R.id.layout_main_activity_content)
    LinearLayout mLayoutMainContent;


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private boolean mFlagWaitDrawerClosed = false;

    private int mPhyScreenWidthInPixel;

    private FragmentManager fm = getSupportFragmentManager();
    private KFragmentHome mHomeFragment;
    private KFragmentArtists mArtistFragment;
    private KFragmentFavorite mFavoriteFragment;

    private int mCurrentSelectedMenuItem;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MENU_SELECTED_ITEM, this.mCurrentSelectedMenuItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            this.mCurrentSelectedMenuItem = savedInstanceState.getInt(KEY_MENU_SELECTED_ITEM);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // Set up Navigation View
        setUpNavigationView();

        loadFragment(FRAGMENT_HOME);

        //Request for needed permission (INTERNET, STORAGE)
        requestPermission();
    }

    /**
     * Request necessary permission for the application from user
     */
    private void requestPermission() {
        String[] requiredPermission = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MODIFY_AUDIO_SETTINGS};

        ActivityCompat.requestPermissions(this, requiredPermission, PERMISSION_AUDIO_RECORD);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_AUDIO_RECORD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do something after user've granted permission
                } else {
                    // TO-DO
                    // Properly handling the case user denies any of the required permissions
                    this.finish();
                }
            }
        }
    }

    private void setUpNavigationView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (mFlagWaitDrawerClosed) {
                    // Reset flag
                    mFlagWaitDrawerClosed = false;
                    switch (mCurrentSelectedMenuItem) {
                        case R.id.nav_my_recording:
                            Intent intent = new Intent(KActivityHome.this, KActivityMyRecording.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_setting:
                            Intent setting = new Intent(KActivityHome.this, KActivitySettings.class);
                            startActivity(setting);
                            break;
                    }
                }
            }
        };
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Get screen width in dp
        Configuration configuration = this.getResources().getConfiguration();
        int mPhysicScreenWidthInDp = configuration.screenWidthDp;
        // Get screen width in pixel
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mPhyScreenWidthInPixel = size.x;

        /**
         * Navigation Drawer width is the minimum (NavDrawerMaxWidth , (ScreenSize — ActionBarSize))
         * where NavDrawerMaxWidth is 320 dp for phones and 400 dp for tablets.
         **/
        mNavigationView.getLayoutParams().width = Utils.convertDpToPixel(this, Math.min(320, mPhysicScreenWidthInDp - 56));
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.kactivity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_search) {
            // get the icon's location on screen to pass through to the search screen
            View searchMenuView = mToolbar.findViewById(R.id.menu_search);
            int[] loc = new int[2];
            searchMenuView.getLocationOnScreen(loc);
            startActivityForResult(KActivitySearch.Companion.createStartIntent(this, loc[0], loc[0] +
                    (searchMenuView.getWidth() / 2)), RC_SEARCH, ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
            searchMenuView.setAlpha(0f);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(int fragmentToShow) {
        switch (fragmentToShow) {
            case FRAGMENT_HOME:
                mToolbar.setTitle("Karaoke Lover");
                mTabLayout.setVisibility(View.GONE);
                if (mHomeFragment == null || !mHomeFragment.isVisible()) {
                    if (mFavoriteFragment != null && mFavoriteFragment.isVisible()) {
                        fm.beginTransaction().hide(mFavoriteFragment).commit();
                    }
                    if (mArtistFragment != null && mArtistFragment.isVisible()) {
                        fm.beginTransaction().hide(mArtistFragment).commit();
                    }
                    if (mHomeFragment != null) {
                        fm.beginTransaction().show(mHomeFragment).commit();
                    } else {
                        Bundle extraData = new Bundle();
                        extraData.putInt(KFragmentHome.KEY_PHYSIC_SCREEN_SIZE, mPhyScreenWidthInPixel);
                        mHomeFragment = new KFragmentHome();
                        mHomeFragment.setArguments(extraData);
                        fm.beginTransaction().add(R.id.main_content, mHomeFragment).commit();
                    }
                }
                break;
            case FRAGMENT_ARTIST:
                mToolbar.setTitle("Artists");
                mTabLayout.setVisibility(View.VISIBLE);
                if (mArtistFragment == null || !mArtistFragment.isVisible()) {
                    if (mFavoriteFragment != null && mFavoriteFragment.isVisible()) {
                        fm.beginTransaction().hide(mFavoriteFragment).commit();
                    }
                    if (mHomeFragment != null && mHomeFragment.isVisible()) {
                        fm.beginTransaction().hide(mHomeFragment).commit();
                    }
                    if (mArtistFragment != null) {
                        fm.beginTransaction().show(mArtistFragment).commit();
                    } else {
                        try {
                            mArtistFragment = KFragmentArtists.class.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        fm.beginTransaction().add(R.id.main_content, mArtistFragment).commit();
                    }
                }
                break;
            case FRAGMENT_FAVORITE:
                mToolbar.setTitle("My Favorites");
                mTabLayout.setVisibility(View.GONE);
                if (mFavoriteFragment == null || !mFavoriteFragment.isVisible()) {
                    if (mArtistFragment != null && mArtistFragment.isVisible()) {
                        fm.beginTransaction().hide(mArtistFragment).commit();
                    }
                    if (mHomeFragment != null && mHomeFragment.isVisible()) {
                        fm.beginTransaction().hide(mHomeFragment).commit();
                    }
                    if (mFavoriteFragment != null) {
                        fm.beginTransaction().show(mFavoriteFragment).commit();
                    } else {
                        try {
                            mFavoriteFragment = KFragmentFavorite.class.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        fm.beginTransaction().add(R.id.main_content, mFavoriteFragment, KFragmentFavorite.Companion.getTAG()).commit();
                    }
                }
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        this.mCurrentSelectedMenuItem = id;

        if (id == R.id.nav_home) {
            loadFragment(FRAGMENT_HOME);
        } else if (id == R.id.nav_my_recording) {
            // Set flag to indicate that this item has been clicked
            mFlagWaitDrawerClosed = true;
        } else if (id == R.id.nav_artists) {
            loadFragment(FRAGMENT_ARTIST);
        } else if (id == R.id.nav_favorite) {
            loadFragment(FRAGMENT_FAVORITE);
        } else if (id == R.id.nav_setting) {
            mFlagWaitDrawerClosed = true;
        } else if (id == R.id.nav_exit) {

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SEARCH:
                // reset the search icon which we hid
                View searchMenuView = mToolbar.findViewById(R.id.menu_search);
                if (searchMenuView != null) {
                    searchMenuView.setAlpha(1f);
                }
                if (resultCode == KActivitySearch.Companion.getRESULT_CODE_SAVE()) {
                    String query = data.getStringExtra(KActivitySearch.Companion.getEXTRA_QUERY());
                    if (TextUtils.isEmpty(query)) return;
                }
                break;
            case KFragmentFavorite.REQUEST_CODE_RELOAD_FAVORITE_LIST:
                if (mFavoriteFragment != null) {
                    mFavoriteFragment.reloadIfNecessary(data);
                }
                break;
        }
    }

    /**
     * Get tab layout for Artist fragment
     *
     * @return tablayout
     */
    public TabLayout getTabLayout() {
        return mTabLayout;
    }
}
