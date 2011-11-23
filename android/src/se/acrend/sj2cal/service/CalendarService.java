package se.acrend.sj2cal.service;

import java.util.Collections;
import java.util.List;

import roboguice.service.RoboIntentService;
import se.acrend.sj2cal.calendar.CalendarHelper;
import se.acrend.sj2cal.content.ProviderHelper;
import se.acrend.sj2cal.content.ProviderTypes;
import se.acrend.sj2cal.model.DbModel;
import se.acrend.sj2cal.model.MessageWrapper.TicketType;
import se.acrend.sj2cal.preference.PrefsHelper;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.inject.Inject;

public class CalendarService extends RoboIntentService {

  private static final String TAG = "CalendarService";
  @Inject
  private CalendarHelper calendarHelper;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private ContentResolver contentResolver;

  public CalendarService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);

    List<Long> eventIds = Collections.emptyList();
    Uri eventUri = null;
    DbModel model = providerHelper.findTicket(intent.getData());

    Log.d(TAG, "Got ticket: " + model.getCode());

    if (prefsHelper.getCalendarId() != -1) {
      Log.d(TAG, "Adding to calendarId: " + prefsHelper.getCalendarId());

      eventIds = calendarHelper.findEvents(model.getCode(), TicketType.Confirmation);

      eventUri = calendarHelper.addEvent(model);
      Log.d(TAG, "Added to calendar: " + model.getCode());
      if (eventUri != null) {
        ContentValues values = new ContentValues();
        values.put(ProviderTypes.Columns.CALENDAR_EVENT_URI, eventUri.toString());
        contentResolver.update(intent.getData(), values, null, null);
      }
    }

    if (prefsHelper.isReplaceTicket() && (eventUri != null)) {
      for (Long id : eventIds) {
        calendarHelper.removeEvent(id);
      }
    }
  }
}
