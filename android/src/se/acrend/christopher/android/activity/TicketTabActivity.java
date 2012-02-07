package se.acrend.christopher.android.activity;

import roboguice.activity.RoboTabActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarHelper;
import se.acrend.christopher.android.activity.help.About;
import se.acrend.christopher.android.activity.setup.ChooseAccount;
import se.acrend.christopher.android.activity.setup.RegisterDevice;
import se.acrend.christopher.android.preference.PrefsHelper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

import com.google.inject.Inject;

public class TicketTabActivity extends RoboTabActivity {

  private static final String TAG = "TicketTabActivity";

  @Inject
  private PrefsHelper prefsHelper;

  final ActionBarHelper actionBarHelper = ActionBarHelper.createInstance(this);

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    actionBarHelper.onCreate(savedInstanceState);

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

  @Override
  protected void onPostCreate(final Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    actionBarHelper.onPostCreate(savedInstanceState);
  }

  private void showWizard() {
    if (PrefsHelper.isShowAbout(getApplicationContext())) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), About.class), 1);
    } else if (prefsHelper.getAccountName() == null) {
      startActivityForResult(new Intent().setClass(getApplicationContext(), ChooseAccount.class), 2);
    } else if (prefsHelper.getRegistrationId() == null) {
      startActivity(new Intent().setClass(getApplicationContext(), RegisterDevice.class));
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    showWizard();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    Log.d(TAG, "Anropar onCreateOptionsMenu");
    MenuInflater inflater = actionBarHelper.getMenuInflater(getMenuInflater());
    inflater.inflate(R.menu.options_menu, menu);

    actionBarHelper.onCreateOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
    case R.id.exit:
      finish();
      return true;
    case R.id.about:
      startActivity(new Intent().setClass(this, About.class));
      return true;
    case R.id.preferences:
      startActivity(new Intent().setClass(this, Preferences.class));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

}
