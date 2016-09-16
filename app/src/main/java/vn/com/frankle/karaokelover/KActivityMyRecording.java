package vn.com.frankle.karaokelover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.frankle.karaokelover.adapters.KAdapterMyRecordings;
import vn.com.frankle.karaokelover.util.FileCompare;
import vn.com.frankle.karaokelover.util.RecordingFileFilter;
import vn.com.frankle.karaokelover.views.HorizontalItemDivider;

public class KActivityMyRecording extends AppCompatActivity {

    @BindView(R.id.recycleview_my_recording)
    RecyclerView mRecyclerView;

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

    private KAdapterMyRecordings.OnItemClickListener onItemClickListner = file -> {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        startActivity(intent);
    };

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

        KAdapterMyRecordings adapter = new KAdapterMyRecordings(this, recordings, onItemClickListner);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new HorizontalItemDivider(this, R.drawable.drawable_divider));
        mRecyclerView.setAdapter(adapter);
    }
}
