package vn.com.frankle.karaokelover.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.util.Utils;

public class KActivityEditRecording extends AppCompatActivity {

    private static final String DEBUG_TAG = KActivityEditRecording.class.getSimpleName();

    private String mCurRecordingFilename;

    @BindView(R.id.edtx_edit_title)
    EditText mEdtxTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recording);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViews();
    }

    /**
     * Initialize activity's view
     */
    private void setupViews() {
        mCurRecordingFilename = getIntent().getStringExtra("TITLE");
        String filenameNoExt = Utils.getFilenameExcludeExtension(mCurRecordingFilename);
        mEdtxTitle.setText(filenameNoExt);
        mEdtxTitle.setSelection(filenameNoExt.length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kactivity_edit_recording, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_save:
                handleSaveRecording();
                break;
        }
        return true;
    }

    /**
     * User save his/her recording
     */
    private void handleSaveRecording() {
        // Rename if user changed filename
        String originalFilename = Utils.getFilenameExcludeExtension(mCurRecordingFilename);
        String newFilename = mEdtxTitle.getText().toString();
        Log.d(DEBUG_TAG, "original: " + originalFilename + ", new: " + newFilename);

        if (!originalFilename.equals(newFilename)) {
            boolean changeName = changeFilename(originalFilename, newFilename);
            Log.d(DEBUG_TAG, "Change filename: result = " + changeName);
        }

        Intent intent = new Intent(this, KActivityMyRecording.class);
        startActivity(intent);

        this.finish();
    }

    /**
     * Rename the recording filename
     *
     * @param currentFilename : current file name
     * @param newFilename     : the new filename
     * @return true : if operation success
     */
    private boolean changeFilename(String currentFilename, String newFilename) {
        File directory = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
        File originalFile = new File(directory, currentFilename + ".wav");
        File renameFile = new File(directory, newFilename + ".wav");
        if (originalFile.exists()) {
            return originalFile.renameTo(renameFile);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        handleDiscardRecording();
    }

    /**
     * User discard the recording
     */
    private void handleDiscardRecording() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard Recording");
        builder.setMessage("This will delete the recording. Are you sure?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            new Thread(discardRecordingFile).run();
            KActivityEditRecording.this.finish();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
        });
        builder.create().show();
    }

    private Runnable discardRecordingFile = new Runnable() {
        @Override
        public void run() {
            File recordFileDir = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
            File recordedFile = new File(recordFileDir, mCurRecordingFilename);

            if (recordedFile.exists()) {
                boolean deleted = recordedFile.delete();
                Log.d(DEBUG_TAG, "Delete recorded file: " + (deleted ? "true" : "false"));
            }
        }
    };
}
