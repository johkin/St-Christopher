package se.acrend.sj2cal.receiver;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.activity.TicketTabActivity;
import se.acrend.sj2cal.content.ProviderHelper;
import se.acrend.sj2cal.content.ProviderTypes;
import se.acrend.sj2cal.intent.Intents;
import se.acrend.sj2cal.model.DbModel;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.service.RegistrationService;
import se.acrend.sj2cal.util.DateUtil;
import se.acrend.sj2cal.util.TimeSource;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.inject.Inject;

public class RegistrationReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "TimerRegistrator";

  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private PowerManager powerManager;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private RegistrationService registrationService;
  @Inject
  private TimeSource timeSource;
  @Inject
  private AlarmManager alarmManager;
  @Inject
  private ConnectivityManager connectivityManager;
  @Inject
  private NotificationManager notificationManager;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);

    context.startService(new Intent(context, RegistrationService.class));

    WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Registration");
    wakeLock.acquire();
    try {

      Log.d(TAG, "Received Intent: " + intent);

      if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
          || ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(intent.getAction())) {

        // Lista alla poster i databasen där ankomst inte har passerat
        String[] args = { new Timestamp(timeSource.getCurrentMillis()).toString() };

        List<DbModel> tickets = providerHelper.findTickets("originalArrival > ? and registered = 0", args);
        if (!tickets.isEmpty()) {
          if (connectivityManager.getBackgroundDataSetting()) {
            for (DbModel model : tickets) {
              registerTimer(context, model);
            }
          } else {
            notifyBackgroundData(context);
          }
        }
      } else if (Intents.DELETE_BOOKING.equals(intent.getAction())) {
        registrationService.deleteBooking(intent.getData());
      } else if (Intents.REGISTER_BOOKING.equals(intent.getAction())) {
        // Kontrollera bakgrundsdata
        if (!connectivityManager.getBackgroundDataSetting()) {
          notifyBackgroundData(context);
          return;
        }

        if (!connectivityManager.getActiveNetworkInfo().isConnected()) {
          Calendar fiveMinutes = Calendar.getInstance();
          fiveMinutes.add(Calendar.MINUTE, 5);

          notifyConnectivity(context, fiveMinutes);

          Log.d(
              TAG,
              "Schemalägger ny registrering för url " + intent.getDataString() + " vid "
                  + DateUtil.formatTime(fiveMinutes));

          PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
          alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutes.getTimeInMillis(), pendingIntent);

          return;
        }

        DbModel model = providerHelper.findTicket(intent.getData());

        registrationService.callRegistration(model);
      } else if (Intents.PREPARE_REGISTRATION.equals(intent.getAction())) {
        DbModel model = providerHelper.findTicket(intent.getData());

        registerTimer(context, model);
      }

    } finally {
      wakeLock.release();
    }

  }

  private void registerTimer(final Context context, final DbModel model) {
    Calendar registrationTime = model.getDeparture().getOriginal();
    registrationTime.add(Calendar.MINUTE, -prefsHelper.getReadAheadMinutes());

    Log.d(TAG, "Schemalägger registrering för id " + model.getId() + " vid " + DateUtil.formatTime(registrationTime));
    Log.d(TAG, "Klockan är nu " + DateUtil.formatTime(DateUtil.createCalendar()));

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(Intents.REGISTER_BOOKING,
        ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId())), PendingIntent.FLAG_ONE_SHOT);
    alarmManager.set(AlarmManager.RTC_WAKEUP, registrationTime.getTimeInMillis(), pendingIntent);
  }

  private void notifyBackgroundData(final Context context) {
    Notification notification = new Notification();
    notification.icon = R.drawable.stat_sys_warning;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Kan inte registrera resa hos server.";
    // TODO Rätt action/class
    Intent notificationIntent = new Intent(context, TicketTabActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, "Kan inte registrera resa.",
        "Bakgrundsdata inte tillåten, ändra inställningarna för att registrera resa hos server.", contentIntent);

    notificationManager.notify(1, notification);
  }

  private void notifyConnectivity(final Context context, final Calendar scheduleTime) {
    Notification notification = new Notification();
    notification.icon = R.drawable.sj2cal_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Saknar koppling till nät vid registrering.";
    Intent notificationIntent = new Intent("Dummy");
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, "Saknar koppling till nät vid registrering.",
        "Har schemalagt nytt försök för registrering vid " + DateUtil.formatTime(scheduleTime), pendingIntent);

    notificationManager.notify(1, notification);
  }
}
