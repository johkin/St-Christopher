package se.acrend.christopher.android.activity;

import roboguice.activity.RoboTabActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.help.About;
import se.acrend.christopher.android.activity.setup.ChooseAccount;
import se.acrend.christopher.android.activity.setup.ChooseCalendar;
import se.acrend.christopher.android.activity.setup.RegisterDevice;
import se.acrend.christopher.android.preference.PrefsHelper;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.google.inject.Inject;

public class TicketTabActivity extends RoboTabActivity {

  @Inject
  private PrefsHelper prefsHelper;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.ticket_tab);

    TabHost tabHost = getTabHost();
    TabHost.TabSpec spec;
    Intent intent;

    intent = new Intent().setClass(this, ComingTicketList.class);
    spec = tabHost.newTabSpec("comingTickets").setIndicator("Kommande").setContent(intent);
    tabHost.addTab(spec);

    intent = new Intent().setClass(this, DoneTicketList.class);
    spec = tabHost.newTabSpec("doneTickets").setIndicator("Utförda").setContent(intent);
    tabHost.addTab(spec);

    tabHost.setCurrentTab(0);

    showWizard();
  }

  private void showWizard() {
    if (PrefsHelper.isShowAbout(getApplicationContext())) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), About.class), 1);
    } else if (prefsHelper.getAccountName() == null) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), ChooseAccount.class), 2);
    } else if (prefsHelper.getRegistrationId() == null) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), RegisterDevice.class), 3);
    } else if (prefsHelper.getCalendarId() == -1) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), ChooseCalendar.class), 4);
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    showWizard();
  }

}
