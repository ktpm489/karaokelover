package vn.com.frankle.karaokelover.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import vn.com.frankle.karaokelover.AppCompatPreferenceActivity;
import vn.com.frankle.karaokelover.R;
import vn.com.frankle.karaokelover.views.widgets.SeekbarPreference;

public class KActivitySettings extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
        if (preference instanceof SeekbarPreference) {
            Log.d("SeekbarPreference", "on change event");
            preference.setSummary(preference.getSummary());
        }
        return false;
    };

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

        private SeekbarPreference seekbarPreference;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            seekbarPreference = (SeekbarPreference) findPreference("pref_key_audio_volume");
            seekbarPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }
}
