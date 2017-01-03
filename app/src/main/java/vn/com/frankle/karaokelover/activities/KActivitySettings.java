package vn.com.frankle.karaokelover.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import vn.com.frankle.karaokelover.AppCompatPreferenceActivity;
import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.views.widgets.SeekbarPreference;

public class KActivitySettings extends AppCompatPreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        public static final String KEY_PREF_HD_RECORD = "pref_key_audio_hd";
        public static final String KEY_PREF_BEAT_VOLUME = "pref_key_audio_volume";
        public static final String KEY_PREF_PREVIEW_IMG_QUALITY = "pref_key_video_preview_hd";
        public static final String KEY_PREF_FEEDBACK = "pref_key_feedback";

        CheckBoxPreference hdRecordPref;
        SeekbarPreference beatVolPref;
        ListPreference prevImgQualityPref;
        Preference feedbackPref;

        private Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
            Log.d("PREFS", "OnPreferenceChangeListener");

            if (preference instanceof SeekbarPreference) {
                Log.d("SeekbarPreference", "on change event");
                preference.setSummary(preference.getSummary());
            } else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(newValue.toString());
                Log.d("PREFS", "index = " + index);
                CharSequence summary = index >= 0
                        ? listPreference.getEntries()[index]
                        : null;
                Log.d("PREFS", "summary = " + summary);
                // Set the summary to reflect the new value.
                preference.setSummary(summary);
                if (newValue.toString().equals("0")) {
                    KApplication.Companion.setHD_PREVIEW_VIDEO_FLAG(false);
                } else {
                    KApplication.Companion.setHD_PREVIEW_VIDEO_FLAG(true);
                }
            }
            return true;
        };

        private Preference.OnPreferenceClickListener mOnFeedbackPrefClickListener = preference -> {
            Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
            feedbackIntent.setType("message/rfc822");
            feedbackIntent.setData(Uri.parse("mailto:"));
            feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lavender.studio2017@gmail.com"});
            StringBuilder feedbackTitle = new StringBuilder("[KaraokeTube][");
            feedbackTitle.append(DeviceName.getDeviceName());
            feedbackTitle.append(" - ");
            feedbackTitle.append(Build.VERSION.RELEASE);
            feedbackTitle.append("] Phản hồi");
            feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackTitle.toString());
            try {
                startActivity(Intent.createChooser(feedbackIntent, "Send feedback email..."));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "There is no email client", Toast.LENGTH_SHORT).show();
            }
            return true;
        };


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            hdRecordPref = (CheckBoxPreference) findPreference(KEY_PREF_HD_RECORD);
            beatVolPref = (SeekbarPreference) findPreference(KEY_PREF_BEAT_VOLUME);
            prevImgQualityPref = (ListPreference) findPreference(KEY_PREF_PREVIEW_IMG_QUALITY);
            feedbackPref = findPreference(KEY_PREF_FEEDBACK);

            hdRecordPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            beatVolPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            prevImgQualityPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            feedbackPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

            // Set default value
            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            mOnPreferenceChangeListener.onPreferenceChange(beatVolPref, sharedPref.getInt(KEY_PREF_BEAT_VOLUME, 30));
            mOnPreferenceChangeListener.onPreferenceChange(prevImgQualityPref, sharedPref.getString(KEY_PREF_PREVIEW_IMG_QUALITY, ""));
            feedbackPref.setOnPreferenceClickListener(mOnFeedbackPrefClickListener);
        }
    }
}
