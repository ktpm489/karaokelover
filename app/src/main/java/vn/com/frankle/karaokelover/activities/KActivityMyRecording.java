package vn.com.frankle.karaokelover.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.adapters.KAdapterMyRecordings;
import vn.com.frankle.karaokelover.util.AnimUtils;
import vn.com.frankle.karaokelover.util.FileCompare;
import vn.com.frankle.karaokelover.util.RecordingFileFilter;
import vn.com.frankle.karaokelover.util.TransitionUtils;
import vn.com.frankle.karaokelover.views.recyclerview.InsetDividerDecoration;
import vn.com.frankle.karaokelover.views.recyclerview.SlideInItemAnimator;

public class KActivityMyRecording extends AppCompatActivity {

    enum LayoutType {
        EMPTY,
        RECORDING_LIST
    }


    @BindView(R.id.recycleview_my_recording)
    RecyclerView mRecyclerView;
    @BindView(R.id.content_no_recording)
    RelativeLayout mLayoutNoRecording;

    private KAdapterMyRecordings adapter;
    private Transition expandCollapse;
    private RecordedFileItemAnimator recordedFileItemAnimator;
    // This array list to store current available recording file
    private List<File> mRecordingList = new ArrayList<>();

    /**
     * We run a transition to expand/collapse recorded item view. Scrolling the RecyclerView while this is
     * running causes issues, so we consume touch events while the transition runs.
     */
    private View.OnTouchListener touchEater = (view, motionEvent) -> true;

    private KAdapterMyRecordings.RecordedSongAdapterListener recordedSongAdapterListener = new KAdapterMyRecordings.RecordedSongAdapterListener() {
        @Override
        public void onItemClick(View holder, File file, int position) {
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
        }

        @Override
        public void onPlayClick(File file) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(KActivityMyRecording.this, "vn.com.frankle.karaokelover", file);
                intent.setDataAndType(contentUri, "audio/*");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
            }
            startActivity(intent);
        }

        @Override
        public void onShareClick(File file) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("*/*");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_send_to)));
        }

        @Override
        public void onDeleteClick(File file, int position) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(KActivityMyRecording.this);
            alertDialogBuilder.setMessage(getResources().getString(R.string.msg_delete_file));
            alertDialogBuilder.setPositiveButton("YES", (dialog, which) -> new DeleteRecordedFileTask(position).execute(file));
            alertDialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.cancel());
            alertDialogBuilder.create().show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_my_recordings);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(KApplication.appResource.getString(R.string.title_activity_kmy_recording));
        }

        loadMyRecordings();
    }

    private void loadMyRecordings() {
        File recordingDir = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
        try {
            File recordings[] = recordingDir.listFiles(new RecordingFileFilter());
            if (recordings != null && recordings.length > 0) {
                setContentLayoutType(LayoutType.RECORDING_LIST);

                Collections.addAll(mRecordingList, recordings);

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

                adapter = new KAdapterMyRecordings(this, recordings, recordedSongAdapterListener);
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
            } else {
                setContentLayoutType(LayoutType.EMPTY);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, KApplication.appResource.getString(R.string.toast_error_reading_directory), Toast.LENGTH_SHORT).show();
        }
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

    /**
     * Task to delete existed recorded file before starting recording
     */
    private class DeleteRecordedFileTask extends AsyncTask<File, Void, Boolean> {
        ProgressDialog deleteProgessDialog = new ProgressDialog(KActivityMyRecording.this);
        private int deletedPos;

        public DeleteRecordedFileTask(int deletedPos) {
            this.deletedPos = deletedPos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            deleteProgessDialog.setMessage(KApplication.appResource.getString(R.string.action_delete_old_recording));
            deleteProgessDialog.setIndeterminate(true);
            deleteProgessDialog.show();
        }

        @Override
        protected Boolean doInBackground(File... params) {

            return params[0].exists() && params[0].delete();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                deleteProgessDialog.dismiss();
                adapter.removeItemAtPosition(deletedPos);
                adapter.setExpandedItemPosition(RecyclerView.NO_POSITION);
                mRecordingList.remove(deletedPos);
                if (mRecordingList.isEmpty()) {
                    setContentLayoutType(LayoutType.EMPTY);
                }
                Toast.makeText(KActivityMyRecording.this, getResources().getString(R.string.toast_delete_file_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(KActivityMyRecording.this, getResources().getString(R.string.toast_delete_file_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setContentLayoutType(LayoutType layoutType) {
        switch (layoutType) {
            case EMPTY:
                mRecyclerView.setVisibility(View.GONE);
                mLayoutNoRecording.setVisibility(View.VISIBLE);
                break;
            case RECORDING_LIST:
                mRecyclerView.setVisibility(View.VISIBLE);
                mLayoutNoRecording.setVisibility(View.GONE);
                break;
        }
    }
}
