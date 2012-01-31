package se.acrend.christopher.android.activity;

import java.sql.Timestamp;
import java.util.TimeZone;

import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.TicketAdapter;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Inject;

public class DoneTicketList extends AbtractTicketList {

  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private GoogleAnalyticsTracker tracker;

  @Override
  protected void onResume() {
    super.onResume();
    tracker.trackPageView("DoneTicketList");
  }

  @Override
  Cursor getCursor() {
    Uri tickets = providerHelper.getTicketsUrl();

    Cursor cursor = getContentResolver().query(tickets, TicketAdapter.PROJECTION, "originalArrival < ?",
        new String[] { new Timestamp(System.currentTimeMillis()
            - TimeZone.getDefault().getOffset(System.currentTimeMillis())).toString() }, "originalDeparture DESC");
    return cursor;
  }
}
