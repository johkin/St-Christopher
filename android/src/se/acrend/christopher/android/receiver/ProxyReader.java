package se.acrend.christopher.android.receiver;

import java.sql.Timestamp;
import java.util.Calendar;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.R;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.trafficinfo.provider.TrafficInfoProviderTypes;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.HttpUtil;
import se.acrend.christopher.android.util.TimeSource;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.inject.Inject;

public class ProxyReader extends RoboBroadcastReceiver {

  private static final String TAG = "ProxyReader";
  private static final String URL = HttpUtil.SERVER_URL + "/proxy?trainNo=%s&date=%s";

  public static final long FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000;
  public static final long ONE_MINUTE_IN_MILLIS = 1 * 60 * 1000;

  @Inject
  private TimeSource timeSource;
  @Inject
  private ConnectivityManager connectivityManager;
  @Inject
  private NotificationManager notificationManager;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private AlarmManager alarmManager;
  @Inject
  private Context context;
  @Inject
  private ContentResolver contentResolver;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);

    if (!connectivityManager.getBackgroundDataSetting()) {
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
      alarmManager.cancel(pendingIntent);
      Log.d(TAG, "Cancel på Timer, tillåter inte bakgrundsdata.");
      return;
    }
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if ((networkInfo == null) || !networkInfo.isConnected()) {
      // Hur hittar man prefs-intent
      Intent backgroundIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
      notify("Ingen aktiv uppkoppling, kan inte kontrollera trafik-information.", backgroundIntent);

      return;
    }

    AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

      @Override
      protected Void doInBackground(final String... params) {

        DbModel model = providerHelper.findTicket(intent.getData());
        StringBuilder builder = new StringBuilder();
        builder.append(TrafficInfoProviderTypes.CONTENT_URI.toString());
        builder.append("/").append(DateUtil.formatDate(model.getDeparture().getOriginal()));
        builder.append("/").append(model.getTrain());
        Uri uri = Uri.parse(builder.toString());
        Log.d(TAG, "Hämtar info för: " + uri);

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null) {
          handleResponse(context, intent, model, cursor);
        }
        return null;
      }

    };
    task.execute(null);
  }

  private void notify(final String message, final Intent intent) {
    Notification notification = new Notification();
    notification.icon = R.drawable.ic_launcher_logo_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = message;
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

    notification.setLatestEventInfo(context, "Trafikinformation.", message, contentIntent);

    notificationManager.notify(1, notification);
  }

  void handleResponse(final Context context, final Intent intent, final DbModel model, final Cursor cursor) {
    boolean departure = false;
    ContentValues values = new ContentValues();
    int stationCol = cursor.getColumnIndex("stationName");
    int infoCol = cursor.getColumnIndex("info");
    int trackCol = cursor.getColumnIndex("track");
    do {
      String stationName = cursor.getString(stationCol);
      if (model.getFrom().equals(stationName)) {
        departure = true;
      } else if (model.getTo().equals(stationName)) {
        departure = false;
      } else {
        continue;
      }
      Log.d(TAG, "Hittade " + stationName);

      if (departure) {
        updateValues(values, ProviderTypes.Columns.DEPARTURE_INFO, cursor.getString(infoCol));
        updateValues(values, ProviderTypes.Columns.DEPARTURE_TRACK, cursor.getString(trackCol));
      } else {
        updateValues(values, ProviderTypes.Columns.ARRIVAL_INFO, cursor.getString(infoCol));
        updateValues(values, ProviderTypes.Columns.ARRIVAL_TRACK, cursor.getString(trackCol));
      }
      Calendar previousEstimate = null;
      int actualCol = 0;
      int calculatedCol = 0;
      int timeCol = 0;
      if (departure) {
        previousEstimate = model.getDeparture().getActual();
        actualCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.DEPARTURE_ACTUAL);
        calculatedCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.DEPARTURE_CALCULATED);
        timeCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.DEPARTURE_TIME);
      } else {
        previousEstimate = model.getArrival().getActual();
        actualCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.ARRIVAL_ACTUAL);
        calculatedCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.ARRIVAL_CALCULATED);
        timeCol = cursor.getColumnIndex(TrafficInfoProviderTypes.Columns.ARRIVAL_TIME);
      }
      Calendar calculated = createCalendar(cursor, calculatedCol);
      Calendar actual = createCalendar(cursor, actualCol);

      Calendar currentEstimate = null;

      if (calculated != null) {
        Log.d(TAG, "Hittade beräknade tid: " + DateUtil.formatDateTime(calculated));
        // Lagra värdet
        currentEstimate = calculated;
      } else if (actual != null) {
        Log.d(TAG, "Hittade faktisk tid: " + DateUtil.formatDateTime(actual));
        // Ta bort pollning
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.cancel(pendingIntent);

        // lagra värdet
        currentEstimate = actual;

      } else {
        currentEstimate = createCalendar(cursor, timeCol);
      }
      if (!currentEstimate.equals(previousEstimate)) {
        // Kontrollera om vi är tillbaka på rätt tid?

        // Intent delayed = null;
        //
        // if (departure) {
        // delayed = new Intent(Intents.DELAYED_DEPARTURE, intent.getData());
        // updateValues(values, ProviderTypes.Columns.ACTUAL_DEPARTURE,
        // currentEstimate);
        // } else {
        // delayed = new Intent(Intents.DELAYED_ARRIVAL, intent.getData());
        // updateValues(values, ProviderTypes.Columns.ACTUAL_ARRIVAL,
        // currentEstimate);
        // }
        //
        // context.sendBroadcast(delayed);
      }

      // Säkerhets koll för att inte polla i evighet

    } while (cursor.moveToNext());

    if (values.size() > 0) {
      contentResolver.update(intent.getData(), values, null, null);
    }
  }

  private Calendar createCalendar(final Cursor cursor, final int calculatedCol) {

    String value = cursor.getString(calculatedCol);
    if (value == null) {
      return null;
    }
    Timestamp timestamp = Timestamp.valueOf(value);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp.getTime());

    return calendar;
  }

  private void updateValues(final ContentValues values, final String key, final String value) {
    if (value == null) {
      return;
    }
    values.put(key, value);
  }

  private void updateValues(final ContentValues values, final String key, final Calendar value) {
    if (value == null) {
      return;
    }
    values.put(key, new Timestamp(value.getTimeInMillis()).toString());
  }

  public void setTimeSource(final TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  public void setConnectivityManager(final ConnectivityManager connectivityManager) {
    this.connectivityManager = connectivityManager;
  }

  public void setNotificationManager(final NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  public void setProviderHelper(final ProviderHelper providerHelper) {
    this.providerHelper = providerHelper;
  }

  public void setAlarmManager(final AlarmManager alarmManager) {
    this.alarmManager = alarmManager;
  }

  public void setContext(final Context context) {
    this.context = context;
  }
}
