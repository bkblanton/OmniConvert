package convert.unitconverter;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by Bryce on 7/24/2015.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        addPreferencesFromResource(R.xml.preferences);
        Preference sigFigsPreference = getPreferenceScreen().findPreference("pref_sigFigs");

        //Validate numbers only
        sigFigsPreference.setOnPreferenceChangeListener(numberCheckListener);
    }

    Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //Check that the string is an integer.
            try {
                Integer.parseInt((String) newValue);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };


}
