package se.acrend.christopher.android.content;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.util.DateUtil;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.inject.Inject;

public class ProviderHelper {

  private static final String TAG = "ProviderHelper";
  @Inject
  private ContentResolver contentResolver;

  public Uri getTicketsUrl() {
    return ProviderTypes.CONTENT_URI;
  }

  public DbModel findTicket(final Uri uri) {
    List<DbModel> list = query(uri, null, null);
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  public List<DbModel> findTickets(final String selection, final String[] selectionArgs) {
    return query(ProviderTypes.CONTENT_URI, selection, selectionArgs);
  }

  private List<DbModel> query(final Uri uri, final String selection, final String[] selectionArgs) {
    String[] projection = { BaseColumns._ID, ProviderTypes.Columns.ORIGINAL_ARRIVAL,
        ProviderTypes.Columns.ACTUAL_ARRIVAL, ProviderTypes.Columns.ESTIMATED_ARRIVAL,
        ProviderTypes.Columns.GUESSED_ARRIVAL, ProviderTypes.Columns.ORIGINAL_DEPARTURE,
        ProviderTypes.Columns.ACTUAL_DEPARTURE, ProviderTypes.Columns.ESTIMATED_DEPARTURE,
        ProviderTypes.Columns.GUESSED_DEPARTURE, ProviderTypes.Columns.FROM, ProviderTypes.Columns.TO,
        ProviderTypes.Columns.TRAIN_NO, ProviderTypes.Columns.TICKET_CODE, ProviderTypes.Columns.TICKET_TEXT,
        ProviderTypes.Columns.REGISTERED,
        ProviderTypes.Columns.NOTIFY, ProviderTypes.Columns.CAR, ProviderTypes.Columns.SEAT,
        ProviderTypes.Columns.DEPARTURE_TRACK, ProviderTypes.Columns.ARRIVAL_TRACK,
        ProviderTypes.Columns.DEPARTURE_INFO, ProviderTypes.Columns.ARRIVAL_INFO,
        ProviderTypes.Columns.CALENDAR_EVENT_URI };
    Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
    List<DbModel> result = new ArrayList<DbModel>();
    while (cursor.moveToNext()) {
      DbModel model = new DbModel();

      model.setId(cursor.getLong(0));
      TimeModel arrival = model.getArrival();
      arrival.setOriginal(createTimestamp(cursor.getString(1)));
      arrival.setActual(createTimestamp(cursor.getString(2)));
      arrival.setEstimated(createTimestamp(cursor.getString(3)));
      arrival.setGuessed(createTimestamp(cursor.getString(4)));
      TimeModel departure = model.getDeparture();
      departure.setOriginal(createTimestamp(cursor.getString(5)));
      departure.setActual(createTimestamp(cursor.getString(6)));
      departure.setEstimated(createTimestamp(cursor.getString(7)));
      departure.setGuessed(createTimestamp(cursor.getString(8)));
      model.setFrom(cursor.getString(9));
      model.setTo(cursor.getString(10));
      model.setTrain(cursor.getString(11));
      model.setCode(cursor.getString(12));
      model.setMessage(cursor.getString(13));
      int checkTraffic = cursor.getInt(14);
      model.setRegistered(checkTraffic == 1);
      int notify = cursor.getInt(15);
      model.setNotify(notify == 1);
      model.setCar(cursor.getString(16));
      model.setSeat(cursor.getString(17));
      model.setDepartureTrack(cursor.getString(18));
      model.setArrivalTrack(cursor.getString(19));
      model.setDepartureInfo(cursor.getString(20));
      model.setArrivalInfo(cursor.getString(21));
      model.setCalendarEventUri(cursor.getString(22));

      result.add(model);
    }
    cursor.close();
    return result;
  }

  private Calendar createTimestamp(final String value) {
    if (value == null) {
      return null;
    }
    Timestamp timestamp = Timestamp.valueOf(value);
    Calendar cal = DateUtil.createCalendar();
    cal.setTime(timestamp);
    return cal;
  }

  public long addEvent(final DbModel model) {
    try {

      ContentValues event = new ContentValues();
      event.put("ticketCode", model.getCode());
      event.put("fromStation", model.getFrom());
      event.put("toStation", model.getTo());
      event.put("trainNo", model.getTrain());
      event.put("originalArrival", new Timestamp(model.getArrival().getOriginal().getTimeInMillis()).toString());
      event.put("originalDeparture", new Timestamp(model.getDeparture().getOriginal().getTimeInMillis()).toString());
      event.put("car", model.getCar());
      event.put("seat", model.getSeat());
      event.put("ticketText", model.getMessage());
      event.put("registered", model.isRegistered());
      event.put("notify", model.isNotify());

      Uri eventsUri = ProviderTypes.CONTENT_URI;
      Uri result = contentResolver.insert(eventsUri, event);
      Log.d(TAG, result.toString());
      return Long.parseLong(result.getLastPathSegment());
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Fel vid lagring till databas.", e);
      throw new RuntimeException("Fel vid lagring till databas.", e);
    }
  }

  public int delete(final Uri data) {
    Log.d(TAG, "Delete Ticket: " + data);
    int count = contentResolver.delete(data, null, null);
    return count;
  }

  public int update(final DbModel ticket) {
    Log.d(TAG, "Uppdaterar biljett " + ticket.getId());
    int count = 0;
    try {
      ContentValues event = new ContentValues();
      event.put("ticketCode", ticket.getCode());
      event.put("fromStation", ticket.getFrom());
      event.put("toStation", ticket.getTo());
      event.put("trainNo", ticket.getTrain());
      event.put("car", ticket.getCar());
      event.put("seat", ticket.getSeat());
      addTimeValues(event, ticket.getArrival(), "Arrival");
      addTimeValues(event, ticket.getDeparture(), "Departure");
      event.put("ticketText", ticket.getMessage());
      event.put("notify", ticket.isNotify());
      event.put("registered", ticket.isRegistered());
      event.put("departureTrack", ticket.getDepartureTrack());
      event.put("arrivalTrack", ticket.getArrivalTrack());
      event.put("departureInfo", ticket.getDepartureInfo());
      event.put("arrivalInfo", ticket.getArrivalInfo());
      Log.d(TAG, "Update CalendarEventUri: " + ticket.getCalendarEventUri());
      event.put("calendarEventUri", ticket.getCalendarEventUri());

      Uri eventsUri = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, ticket.getId());
      count = contentResolver.update(eventsUri, event, null, null);
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Fel vid lagring till databas.", e);
      throw new RuntimeException("Fel vid lagring till databas.", e);
    }
    return count;
  }

  private void addTimeValues(final ContentValues values, final TimeModel model, final String suffix) {
    values.put("original" + suffix, new Timestamp(model.getOriginal().getTimeInMillis()).toString());
    if (model.getActual() != null) {
      values.put("actual" + suffix, new Timestamp(model.getActual().getTimeInMillis()).toString());
    }
    if (model.getEstimated() != null) {
      values.put("estimated" + suffix, new Timestamp(model.getEstimated().getTimeInMillis()).toString());
    }
    if (model.getGuessed() != null) {
      values.put("guessed" + suffix, new Timestamp(model.getGuessed().getTimeInMillis()).toString());
    }
  }

  public DbModel findByCode(final String code) {
    List<DbModel> result = query(ProviderTypes.CONTENT_URI, ProviderTypes.Columns.TICKET_CODE + " = ?",
        new String[] { code });
    if (result.isEmpty()) {
      return null;
    }
    return result.get(0);

  }
}
