package tu.dresden.studybloxx.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import tu.dresden.studybloxx.R;

/**
 * Created by Arjun Naik<arjun@arjunnaik.in> on 16.03.14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
