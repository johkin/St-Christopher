package se.acrend.christopher.android.activity;

import java.sql.Timestamp;
import java.util.TimeZone;

import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.TicketAdapter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Inject;

public class ComingTicketList extends AbtractTicketList {

  private static final String TAG = "ComingTicketList";

  @Inject
  ProviderHelper providerHelper;
  @Inject
  private GoogleAnalyticsTracker tracker;

  @Override
  protected void onResume() {
    super.onResume();
    tracker.trackPageView("ComingTicketList");
  }

  @Override
  protected Cursor getCursor() {
    Uri tickets = providerHelper.getTicketsUrl();

    Log.d(TAG, "Local: " + new Timestamp(System.currentTimeMillis()).toString());
    Log.d(TAG, "UTC: " + new Timestamp(System.currentTimeMillis()
        - TimeZone.getDefault().getOffset(System.currentTimeMillis())).toString());

    Cursor cursor = getContentResolver().query(
        tickets,
        TicketAdapter.PROJECTION,
        "originalArrival > ?",
        new String[] { new Timestamp(System.currentTimeMillis()
            - TimeZone.getDefault().getOffset(System.currentTimeMillis())).toString() }, "originalDeparture ASC");
    return cursor;
  }
}
