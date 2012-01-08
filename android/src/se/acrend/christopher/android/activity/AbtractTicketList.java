package se.acrend.christopher.android.activity;

import roboguice.activity.RoboListActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.content.TicketAdapter;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public abstract class AbtractTicketList extends RoboListActivity {

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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

}