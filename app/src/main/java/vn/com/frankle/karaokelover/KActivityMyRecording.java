package vn.com.frankle.karaokelover;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;

import java.io.File;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.adapters.KAdapterMyRecordings;
import vn.com.frankle.karaokelover.util.AnimUtils;
import vn.com.frankle.karaokelover.util.FileCompare;
import vn.com.frankle.karaokelover.util.RecordingFileFilter;
import vn.com.frankle.karaokelover.util.TransitionUtils;
import vn.com.frankle.karaokelover.views.recyclerview.InsetDividerDecoration;
import vn.com.frankle.karaokelover.views.recyclerview.SlideInItemAnimator;

public class KActivityMyRecording extends AppCompatActivity {

    @BindView(R.id.recycleview_my_recording)
    RecyclerView mRecyclerView;
    private KAdapterMyRecordings adapter;
    private Transition expandCollapse;
    private RecordedFileItemAnimator recordedFileItemAnimator;

    /**
     * We run a transition to expand/collapse recorded item view. Scrolling the RecyclerView while this is
     * running causes issues, so we consume touch events while the transition runs.
     */
    private View.OnTouchListener touchEater = (view, motionEvent) -> true;

    private KAdapterMyRecordings.OnItemClickListener onRecordedItemClickListener = (holder, file, position) -> {
        if (position == RecyclerView.NO_POSITION) return;

        TransitionManager.beginDelayedTransition(mRecyclerView, expandCollapse);
        recordedFileItemAnimator.setAnimateMoves(false);

        // collapse any currently expanded items
        if (adapter.getExpandedItemPosition() != RecyclerView.NO_POSITION) {
            adapter.notifyItemChanged(adapter.getExpandedItemPosition(), KAdapterMyRecordings.COLLAPSE);
        }

        // expand this item (if it wasn't already)
        if (adapter.getExpandedItemPosition() != position) {
            adapter.setExpandedItemPosition(position);

            mRecyclerView.getAdapter().notifyItemChanged(position, KAdapterMyRecordings.EXPAND);
            holder.requestFocus();
        } else {
            adapter.setExpandedItemPosition(RecyclerView.NO_POSITION);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_my_recordings);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadMyRecordings();
    }

    private void loadMyRecordings() {
        File recordingDir = new File(KApplication.RECORDING_DIRECTORY_URI);


        File recordings[] = recordingDir.listFiles(new RecordingFileFilter());
        FileCompare[] pairs = new FileCompare[recordings.length];
        for (int i = 0; i < recordings.length; i++) {
            pairs[i] = new FileCompare(recordings[i]);
        }
        // Sort list of recording based on last modified date
        // The latest modified file is displayed first
        Arrays.sort(pairs);
        for (int i = 0; i < recordings.length; i++) {
            recordings[i] = pairs[i].file;
        }

        expandCollapse = new AutoTransition();
        expandCollapse.setDuration(getResources().getInteger(R.integer.recorded_item_expand_collapse_duration));
        expandCollapse.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(KActivityMyRecording.this));
        expandCollapse.addListener(new TransitionUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(Transition transition) {
                mRecyclerView.setOnTouchListener(touchEater);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                recordedFileItemAnimator.setAnimateMoves(true);
                mRecyclerView.setOnTouchListener(null);
            }
        });

        adapter = new KAdapterMyRecordings(this, recordings, onRecordedItemClickListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new InsetDividerDecoration(
                KAdapterMyRecordings.ViewHolderRecording.class,
                getResources().getDimensionPixelSize(R.dimen.divider_height),
                getResources().getDimensionPixelSize(R.dimen.keyline_1),
                ContextCompat.getColor(this, R.color.divider_light)));

        recordedFileItemAnimator = new RecordedFileItemAnimator();
        mRecyclerView.setItemAnimator(recordedFileItemAnimator);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * A {@link RecyclerView.ItemAnimator} which allows disabling move animations. RecyclerView
     * does not like animating item height changes. {@link android.transition.ChangeBounds} allows
     * this but in order to simultaneously collapse one item and expand another, we need to run the
     * Transition on the entire RecyclerView. As such it attempts to move views around. This
     * custom item animator allows us to stop RecyclerView from trying to handle this for us while
     * the transition is running.
     */
    /* package */ static class RecordedFileItemAnimator extends SlideInItemAnimator {

        private boolean animateMoves = false;

        RecordedFileItemAnimator() {
            super();
        }

        void setAnimateMoves(boolean animateMoves) {
            this.animateMoves = animateMoves;
        }

        @Override
        public boolean animateMove(
                RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            if (!animateMoves) {
                dispatchMoveFinished(holder);
                return false;
            }
            return super.animateMove(holder, fromX, fromY, toX, toY);
        }
    }
}
