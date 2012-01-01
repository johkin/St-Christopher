package se.acrend.christopher.android.activity;

import roboguice.activity.RoboListActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarHelper;
import se.acrend.christopher.android.activity.help.About;
import se.acrend.christopher.android.content.TicketAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public abstract class AbtractTicketList extends RoboListActivity {

  final ActionBarHelper actionBarHelper = ActionBarHelper.createInstance(this);

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    actionBarHelper.onCreate(savedInstanceState);

    setContentView(R.layout.ticket_list);

    readDatabase();
  }

  @Override
  protected void onResume() {
    super.onResume();

    readDatabase();
  }

  private void readDatabase() {
    Cursor cursor = getCursor();
    startManagingCursor(cursor);

    SimpleCursorAdapter adapter = new TicketAdapter(getApplicationContext(), cursor);
    setListAdapter(adapter);
  }

  abstract Cursor getCursor();

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = actionBarHelper.getMenuInflater(getMenuInflater());
    inflater.inflate(R.menu.options_menu, menu);

    actionBarHelper.onCreateOptionsMenu(menu);
    super.onCreateOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    // Handle item selection
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