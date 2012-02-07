package se.acrend.christopher.android.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import roboguice.service.RoboService;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.SubscriptionDetails;
import se.acrend.christopher.android.activity.TicketDetails;
import se.acrend.christopher.android.activity.TicketTabActivity;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.service.ServerCommunicationHelper.ResponseCallback;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.HttpUtil;
import se.acrend.christopher.android.util.TimeSource;
import se.acrend.christopher.android.widget.TicketWidgetProvider;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.BookingInformation;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.parser.ParserFactory;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class RegistrationService extends RoboService {

  private static final String TAG = "RegistrationService";
  @Inject
  private ServerCommunicationHelper communicationHelper;
  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private TimeSource timeSource;
  @Inject
  private ConnectivityManager connectivityManager;
  @Inject
  private AlarmManager alarmManager;
  @Inject
  private Context context;
  @Inject
  private NotificationManager notificationManager;

  private static final int MAX_RETRY_COUNT = 5;

  @Override
  public void onStart(final Intent intent, final int startId) {
    super.onStart(intent, startId);

    if (intent == null) {
      return;
    }
    AsyncTask<Void, Void, Void> intentTask = new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(final Void... params) {
        runIntent(intent);
        return null;
      }
    };

    intentTask.execute();
  }

  private void runIntent(final Intent intent) {

    if (Intents.DELETE_BOOKING.equals(intent.getAction())) {
      deleteBooking(intent.getData());
    } else if (Intents.REGISTER_BOOKING.equals(intent.getAction())) {
      int retryCount = intent.getIntExtra("retryCount", 0);
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
                + DateUtil.formatDateTime(fiveMinutes));

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutes.getTimeInMillis(), pendingIntent);

        return;
      }

      DbModel model = providerHelper.findTicket(intent.getData());

      callRegistration(model, retryCount);
    } else if (Intents.PREPARE_REGISTRATION.equals(intent.getAction())) {
      if (intent.getData() != null) {
        DbModel model = providerHelper.findTicket(intent.getData());

        registerTimer(context, model);
      } else {
        updatePendingRegistrations();
      }
    }
  }

  public void updatePendingRegistrations() {
    if (!connectivityManager.getBackgroundDataSetting()) {
      notifyBackgroundData(context);
    } else {
      // Lista alla poster i databasen där ankomst inte har passerat
      String[] args = { new Timestamp(timeSource.getCurrentMillis()).toString() };

      List<DbModel> tickets = providerHelper.findTickets("originalArrival > ? and registered = 0", args);
      if (!tickets.isEmpty()) {

        for (DbModel model : tickets) {
          registerTimer(context, model);
        }
      }
    }
  }

  public void deleteBooking(final Uri data) {
    Log.d(TAG, "Delete booking");

    try {
      DbModel model = providerHelper.findTicket(data);

      callUnRegister(model);

      providerHelper.delete(data);

      context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
          TicketWidgetProvider.class));
    } catch (TemporaryException e) {
      Log.e(TAG, "Error in delete.", e);
    }
  }

  private void callUnRegister(final DbModel info) {
    Log.d(TAG, "UnRegister booking");

    HttpPost post = communicationHelper.createPostRequest(HttpUtil.REGISTRATION_PATH + "/unregister");

    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("code", info.getCode()));

    boolean result = communicationHelper.callServer(post, nameValuePairs, new ResponseCallback<Boolean>() {

      @Override
      public Boolean doWithResponse(final HttpResponse response) throws IOException {
        return HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
      }
    });

    if (result) {
      info.setRegistered(false);
    }

    providerHelper.update(info);
  }

  public boolean callRegistration(final DbModel model, final int retryCount) {
    Log.d(TAG, "Registrera bokning");

    try {
      String registrationId = prefsHelper.getRegistrationId();

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.REGISTRATION_PATH + "/register");

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("code", model.getCode()));
      nameValuePairs.add(new BasicNameValuePair("trainNo", model.getTrain()));
      nameValuePairs.add(new BasicNameValuePair("from", model.getFrom()));
      nameValuePairs.add(new BasicNameValuePair("to", model.getTo()));
      nameValuePairs.add(new BasicNameValuePair("date", DateUtil.formatDate(model.getDeparture().getOriginal())));
      nameValuePairs.add(new BasicNameValuePair("registrationId", registrationId));

      BookingInformation information = communicationHelper.callServer(post, nameValuePairs,
          new ResponseCallback<BookingInformation>() {

            @Override
            public BookingInformation doWithResponse(final HttpResponse response) throws IOException {
              Gson gson = ParserFactory.createParser();
              BookingInformation information = gson.fromJson(new InputStreamReader(response.getEntity().getContent()),
                  BookingInformation.class);

              return information;
            }

          });

      if (information.getReturnCode() == ReturnCode.Success) {
        model.setRegistered(true);

        model.setDepartureTrack(information.getDepartureTrack());
        model.setArrivalTrack(information.getArrivalTrack());

        TimeModel departure = model.getDeparture();
        departure.setOriginal(information.getOriginalDeparture());
        departure.setActual(information.getActualDeparture());
        departure.setEstimated(information.getEstimatedDeparture());
        departure.setGuessed(information.getGuessedDeparture());
        departure.setInfo(information.getDepartureInfo());

        TimeModel arrival = model.getArrival();
        arrival.setOriginal(information.getOriginalArrival());
        arrival.setActual(information.getActualArrival());
        arrival.setEstimated(information.getEstimatedArrival());
        arrival.setGuessed(information.getGuessedArrival());
        arrival.setInfo(information.getArrivalInfo());
      } else {
        Log.w(TAG, "Tog emot fel från server: " + information.getErrorCode());
        model.setRegistered(false);
        ErrorCode errorCode = information.getErrorCode();
        if (errorCode == ErrorCode.UpdateNotificationSubscription) {
          Notification notification = new Notification();
          notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS
              | Notification.DEFAULT_VIBRATE;
          notification.icon = R.drawable.ic_launcher_logo_bw;
          notification.when = System.currentTimeMillis();
          notification.flags = Notification.FLAG_AUTO_CANCEL;
          notification.tickerText = "Saknar giltig prenumeration.";

          Intent notificationIntent = new Intent(context, SubscriptionDetails.class);
          notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

          notification.setLatestEventInfo(context, "Saknar giltig prenumeration.",
              "Uppdatera din prenumeration för att kunna registrera din resa.", contentIntent);

          notificationManager.notify(model.getTrain(), 1, notification);
        } else if (errorCode == ErrorCode.TrainNotFound) {
          Notification notification = new Notification();
          notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS
              | Notification.DEFAULT_VIBRATE;
          notification.icon = R.drawable.ic_launcher_logo_bw;
          notification.when = System.currentTimeMillis();
          notification.flags = Notification.FLAG_AUTO_CANCEL;
          notification.tickerText = "Kunde inte hitta information om tåg " + model.getTrain();

          Intent notificationIntent = new Intent(context, SubscriptionDetails.class);
          notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

          notification.setLatestEventInfo(context, "Tåg-information saknas.",
              "Servern kunde inte hämta information för det tåg som angivits i biljetten.", contentIntent);

          notificationManager.notify(model.getTrain(), 1, notification);
        } else {
          scheduleNewRegistration(model, retryCount);
        }
      }

      providerHelper.update(model);

      context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
          TicketWidgetProvider.class));
    } catch (TemporaryException e) {
      Log.e(TAG, "Error in callRegistration.", e);
      scheduleNewRegistration(model, retryCount);
    }
    return model.isRegistered();
  }

  private void scheduleNewRegistration(final DbModel model, final int retryCount) {
    Uri data = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId());
    if (retryCount > MAX_RETRY_COUNT) {
      Log.e(TAG, "Har uppnått max antal försök, avbryter registrering.");
      notifyMaxRetry(context, data);
      return;
    }
    Calendar fiveMinutes = Calendar.getInstance();
    fiveMinutes.add(Calendar.MINUTE, 5);

    Log.d(TAG, "Schemalägger ny registrering för id " + model.getId() + " vid " + DateUtil.formatDateTime(fiveMinutes));
    Intent intent = new Intent(Intents.REGISTER_BOOKING, data);
    intent.putExtra("retryCount", retryCount + 1);

    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutes.getTimeInMillis(), pendingIntent);
  }

  private void notifyConnectivity(final Context context, final Calendar scheduleTime) {
    Notification notification = new Notification();
    notification.icon = R.drawable.ic_launcher_logo_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Saknar koppling till nät vid registrering.";
    // TODO Intent för nätverk?
    Intent notificationIntent = new Intent("Dummy");
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, "Saknar koppling till nät vid registrering.",
        "Har schemalagt nytt försök för registrering vid " + DateUtil.formatDateTime(scheduleTime), pendingIntent);

    notificationManager.notify(1, notification);
  }

  private void notifyMaxRetry(final Context context, final Uri data) {
    Notification notification = new Notification();
    notification.icon = R.drawable.ic_launcher_logo_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Max antal försök till registrering har uppnåtts.";
    Intent notificationIntent = new Intent(context, TicketDetails.class).setData(data);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification
        .setLatestEventInfo(
            context,
            "Fel vid registrering.",
            "Ett fel inträffade vid registrering av din bokning, max antal försök har uppnåtts. Välj att registrera din bokning manuellt.",
            pendingIntent);

    notificationManager.notify(1, notification);
  }

  private void notifyBackgroundData(final Context context) {
    Notification notification = new Notification();
    notification.icon = R.drawable.stat_sys_warning;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Kan inte registrera resa hos server.";
    // TODO Intent för bakgrundsdata?
    Intent notificationIntent = new Intent(context, TicketTabActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, "Kan inte registrera resa.",
        "Bakgrundsdata inte tillåten, ändra inställningarna för att registrera resa hos server.", contentIntent);

    notificationManager.notify(1, notification);
  }

  private void registerTimer(final Context context, final DbModel model) {
    Calendar registrationTime = model.getDeparture().getOriginal();
    registrationTime.add(Calendar.MINUTE, -prefsHelper.getReadAheadMinutes());

    Log.d(TAG,
        "Schemalägger registrering för id " + model.getId() + " vid " + DateUtil.formatDateTime(registrationTime));
    Log.d(TAG, "Klockan är nu " + DateUtil.formatDateTime(DateUtil.createCalendar()));

    PendingIntent pendingIntent = PendingIntent.getService(context, 0,
        new Intent(Intents.REGISTER_BOOKING, ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId())),
        PendingIntent.FLAG_ONE_SHOT);
    alarmManager.set(AlarmManager.RTC_WAKEUP, registrationTime.getTimeInMillis(), pendingIntent);
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }
}
