package se.acrend.christopher.android.activity;

import roboguice.activity.RoboPreferenceActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.android.preference.AccountHelper;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.google.inject.Inject;

public class Preferences extends RoboPreferenceActivity {

  @Inject
  private AccountHelper accountHelper;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    ListPreference account = (ListPreference) findPreference("account");

    initAccount(account);

    ListPreference readAheadMinutes = (ListPreference) findPreference("readAheadMinutes");

    readAheadMinutes.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        startService(new Intent(Intents.PREPARE_REGISTRATION));
        return true;
      }
    });
  }

  void initAccount(final ListPreference account) {

    String[] accountNames = accountHelper.getAccountNames();
    account.setEntries(accountNames);
    account.setEntryValues(accountNames);

    account.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        accountHelper.initAccount((String) newValue, Preferences.this, null);
        return true;
      }
    });
  }
}
