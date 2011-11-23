package se.acrend.sj2cal.activity;

import se.acrend.sj2cal.content.ProviderHelper;
import se.acrend.sj2cal.content.TicketAdapter;
import android.database.Cursor;
import android.net.Uri;

import com.google.inject.Inject;

public class ComingTicketList extends AbtractTicketList {

  @Inject
  ProviderHelper providerHelper;

  @Override
  protected Cursor getCursor() {
    Uri tickets = providerHelper.getTicketsUrl();

    Cursor cursor = getContentResolver().query(tickets, TicketAdapter.PROJECTION, "originalArrival > datetime('now')",
        null,
        "originalDeparture ASC");
    return cursor;
  }
}
