package se.acrend.sj2cal.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import se.acrend.sj2cal.model.DbModel;
import se.acrend.sj2cal.model.MessageWrapper.TicketType;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.util.DateUtil;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.google.inject.Inject;

public class CalendarHelper {

  private static final String TAG = "CalendarHelper";

  private static final int level = Build.VERSION.SDK_INT;

  @Inject
  private Context context;

  @Inject
  private PrefsHelper prefsHelper;

  private static Uri eventsUri = null;
  private static Uri calendarsUri = null;

  static {
    eventsUri = Uri.parse(getBaseUrl() + "events/");
    calendarsUri = Uri.parse(getBaseUrl() + "calendars/");
  }

  public static Uri getEventsUri() {
    return eventsUri;
  }

  public static Uri getCalendarsUri() {
    return calendarsUri;
  }

  private static String getBaseUrl() {
    if (level < 8) {
      return "content://calendar/";
    } else {
      return "content://com.android.calendar/";
    }
  }

  public BaseAdapter getCalendarList() {

    String[] projection = new String[] { "_id", "name", "ownerAccount", "color" };

    Cursor managedCursor = context.getContentResolver().query(calendarsUri, projection,
        "selected=1 and access_level=700", null, null);

    BaseAdapter adapter = null;

    if ((managedCursor != null) && managedCursor.moveToFirst()) {
      adapter = new CalendarSpinnerAdapter(context, managedCursor);
    } else {
      Toast.makeText(context, "Hittade ingen kalender", Toast.LENGTH_LONG).show();
    }
    return adapter;
  }

  public Uri addEvent(final DbModel ticket) {
    long calendarId = prefsHelper.getCalendarId();
    if (calendarId == -1) {
      return null;
    }
    ContentValues event = new ContentValues();
    event.put("calendar_id", calendarId);
    String title = "Tågresa";
    if (ticket.getCar() != null) {
      title += ", vagn " + ticket.getCar();
    }
    if (ticket.getSeat() != null) {
      title += ", plats " + ticket.getSeat();
    }
    if ("Tågresa".equals(title)) {
      title += " utan platsbokning";
    }
    event.put("title", title);

    TimeZone timeZone = ticket.getDeparture().getOriginal().getTimeZone();

    event.put("eventLocation", ticket.getFrom());
    event.put("dtstart", ticket.getDeparture().getOriginal().getTimeInMillis());
    event.put("dtend", ticket.getArrival().getOriginal().getTimeInMillis());
    event.put("eventTimezone", timeZone.getID());
    event.put("eventStatus", 1);
    event.put("visibility", 0);

    String desc = ticket.getMessage();

    if (ticket.getTrain() != null) {

      String date = DateUtil.formatDate(ticket.getDeparture().getOriginal());
      String url = String.format("http://www5.trafikverket.se/taginfo/WapPages/TrainShow.aspx?train=%s,%s", date,
          ticket.getTrain());
      desc += "\n\n" + url + "\n\n" + TicketType.SmsTicket.getDisplayName();
    } else {
      desc += "\n\n" + TicketType.Confirmation.getDisplayName();
    }

    event.put("description", desc);

    Uri resultUri = context.getContentResolver().insert(eventsUri, event);

    Log.d(TAG, "Lagt till kalenderpost: " + resultUri);

    return resultUri;
  }

  public List<Long> findEvents(final String ticketCode, final TicketType ticketType) {

    long calendarId = prefsHelper.getCalendarId();
    if (calendarId == -1) {
      return Collections.emptyList();
    }

    String[] projection = new String[] { "_id" };

    Cursor managedCursor = context.getContentResolver().query(eventsUri, projection,
        "calendar_id = ? and description like ?",
        new String[] { Long.toString(calendarId), "%" + ticketCode + "%" + ticketType.getDisplayName() + "%" }, null);

    if (managedCursor == null) {
      return Collections.emptyList();
    }

    List<Long> eventIds = new ArrayList<Long>();
    int colIndex = managedCursor.getColumnIndex(projection[0]);
    while (managedCursor.moveToNext()) {
      eventIds.add(managedCursor.getLong(colIndex));
    }

    return eventIds;
  }

  public boolean removeEvent(final long eventId) {
    int count = 0;
    count = context.getContentResolver().delete(ContentUris.withAppendedId(eventsUri, eventId), null, null);
    return count > 0;
  }
}
