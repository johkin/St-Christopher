package se.acrend.christopher.android.activity;

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

    Cursor cursor = getContentResolver().query(tickets, TicketAdapter.PROJECTION, "originalArrival < datetime('now')",
        null, "originalDeparture DESC");
    return cursor;
  }
}
