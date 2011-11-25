package se.acrend.christopher.android.activity;

import roboguice.activity.RoboPreferenceActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.calendar.CalendarHelper;
import se.acrend.christopher.android.calendar.CalendarListPreference;
import se.acrend.christopher.android.preference.AccountHelper;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.google.inject.Inject;

public class Preferences extends RoboPreferenceActivity {

  @Inject
  private CalendarHelper calendarHelper;
  @Inject
  private Context context;
  @Inject
  private AccountHelper accountHelper;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    CalendarListPreference calendarList = (CalendarListPreference) findPreference("calendarId");
    calendarList.setAdapter(calendarHelper.getCalendarList());

    ListPreference account = (ListPreference) findPreference("account");

    initAccount(account);

    // TODO Preference som pekar på köp-vy

    // TODO Lägg kontroll av OS-version för att avgöra vad som ska vara aktivt,
    // ställ in default-värden.

    // TODO Lägg till möjlighet att byta konto för inloggning, registrering.

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
