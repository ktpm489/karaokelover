package vn.com.frankle.karaokelover.views.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.github.ybq.parallaxviewpager.ParallaxViewPager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by duclm on 16-Dec-16.
 */

public class StateParallaxViewPager extends ParallaxViewPager {
    public static final int TAG_RESTORE_VIEWPAGER_STATE_POS = "TAG_RESTORE_VIEWPAGER_STATE_POS".hashCode();
    public static final String BUNDLE_VIEW_PAGER_POS = "pos";
    public static final String BUNDLE_VIEW_STATE = "viewstate";
    public static final String BUNDLE_PAGER_VIEW_STATE = "pagerviewstate";
    public static final String BUNDLE_CHILDREN_VIEW_STATES = "childrenviewstates";
    private HashMap<Integer, SparseArray<Parcelable>> mSavedViewStates = new HashMap();

    public StateParallaxViewPager(Context context) {
        super(context);
    }

    public StateParallaxViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setViewPositionTag(View v, int position) {
        v.setTag(TAG_RESTORE_VIEWPAGER_STATE_POS, position);
    }

    private int getViewPositionFromTag(View v) {
        Object tag = v.getTag(TAG_RESTORE_VIEWPAGER_STATE_POS);
        return Integer.valueOf(tag.toString());
    }

    /**
     * Called when you init your View for your ViewPager.
     * <p>
     * Either restores state if we have any, or gives the view a positional number TAG so
     * when we restore all the adapters view, we know which ones to put where.
     *
     * @param position
     * @param view
     */
    public void restoreViewState(int position, View view) {
        SparseArray<Parcelable> state = mSavedViewStates.get(position);
        if (state == null) {
            Log.d(StateParallaxViewPager.class.getSimpleName(), "State for view restore was null");
        } else {
            try {
                view.restoreHierarchyState(state);
            } catch (Exception e) {
                Log.d(StateParallaxViewPager.class.getSimpleName(), "Unable to restore view: " + e.getMessage());
            }
        }
        setViewPositionTag(view, position);
    }

    /**
     * Called when the ViewPager's adapter destroys the view -- we'll want to restore its state.
     *
     * @param position
     * @param view
     */
    public void saveViewState(int position, View view) {
        SparseArray<Parcelable> hs = new SparseArray<>();
        view.saveHierarchyState(hs);
        mSavedViewStates.put(position, hs);
    }

    /**
     * Iterates through all the views in the ViewPager,
     * grabs their positional tag if exists
     * and then stores their state in a bundle to be retrieved in onRestoreInstanceState().
     *
     * @return
     */
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable(BUNDLE_PAGER_VIEW_STATE, super.onSaveInstanceState());

        final ArrayList<Parcelable> viewStates = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            final Bundle bundle = new Bundle();
            View v = getChildAt(i);
            Object tag = v.getTag(TAG_RESTORE_VIEWPAGER_STATE_POS);
            if (tag != null) {
                try {
                    bundle.putInt(BUNDLE_VIEW_PAGER_POS, getViewPositionFromTag(v));
                    SparseArray<Parcelable> hierarchyState = new SparseArray<>();
                    v.saveHierarchyState(hierarchyState);
                    bundle.putSparseParcelableArray(BUNDLE_VIEW_STATE, hierarchyState);
                    viewStates.add(bundle);
                } catch (Exception e) {
                    Log.d(StateParallaxViewPager.class.getSimpleName(), "View probably didn't have a proper positional tag", e);
                }
            }
        }
        b.putParcelableArrayList(BUNDLE_CHILDREN_VIEW_STATES, viewStates);

        return b;
    }

    /**
     * From the Parcelable, grabs the view states, by position, and
     * sets the mSavedViewStates -- so restoreViewState() has something to use.
     *
     * @param state
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle)) {
            super.onRestoreInstanceState(state);
        } else {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_PAGER_VIEW_STATE));
            ArrayList<Parcelable> viewStates = bundle.getParcelableArrayList(BUNDLE_CHILDREN_VIEW_STATES);
            if (viewStates == null) {
                Log.d(StateParallaxViewPager.class.getSimpleName(), "No view states to restore");
                return;
            }
            for (int i = 0; i < viewStates.size(); i++) {
                Bundle stateForChild = (Bundle) viewStates.get(i);
                final SparseArray<Parcelable> childState = stateForChild.getSparseParcelableArray(BUNDLE_VIEW_STATE);
                mSavedViewStates.put(stateForChild.getInt(BUNDLE_VIEW_PAGER_POS), childState);
            }

        }
    }

}
