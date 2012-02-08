package se.acrend.christopher.android.activity;

import roboguice.activity.RoboListActivity;
import se.acrend.christopher.R;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.content.TicketAdapter;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public abstract class AbstractTicketList extends RoboListActivity {

  public static final long FOUR_HOURS_IN_MILLIS = 4 * 60 * 60 * 1000;

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

  @Override
  protected void onListItemClick(final ListView l, final View v, final int position, final long id) {

    Uri data = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, id);
    Intent intent = new Intent();
    intent.setData(data);
    intent.setClass(getApplicationContext(), TicketDetails.class);
    startActivity(intent);
  }

}