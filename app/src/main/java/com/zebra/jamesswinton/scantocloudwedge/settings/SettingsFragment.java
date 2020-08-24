package com.zebra.jamesswinton.scantocloudwedge.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.zebra.jamesswinton.scantocloudwedge.R;

public class SettingsFragment extends PreferenceFragmentCompat {

  public SettingsFragment() { }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
    initBaseUrlFormatValidator();
    initRelativeEndpointFormatValidator();
  }

  private void initBaseUrlFormatValidator() {
    Preference baseUrlPreference = getPreferenceScreen().findPreference(getString(R.string.base_url_pref_key));
    if (baseUrlPreference != null) {
      baseUrlPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        String baseUrl = (String) newValue;
        if (!baseUrl.endsWith("/")) {
          Toast.makeText(getContext(), "Url must end with '/'", Toast.LENGTH_LONG).show();
          return false;
        } else if (!baseUrl.contains(":")) {
          Toast.makeText(getContext(), "Url must contain ':'", Toast.LENGTH_LONG).show();
          return false;
        } else {
          return true;
        }
      });
    }
  }

  private void initRelativeEndpointFormatValidator() {
    Preference relativeEndpointPreference = getPreferenceScreen().findPreference(getString(R.string.relative_endpoint_pref_key));
    if (relativeEndpointPreference != null) {
      relativeEndpointPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        String relativeEndpoint = (String) newValue;
        if (relativeEndpoint.startsWith("/")) {
          return !getPreferenceManager()
                  .getSharedPreferences()
                  .edit()
                  .putString(getString(R.string.relative_endpoint_pref_key), relativeEndpoint.substring(1))
                  .commit();
        } else {
          return true;
        }
      });
    }
  }

}
