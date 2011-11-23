package se.acrend.sj2cal.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import roboguice.service.RoboService;
import se.acrend.christopher.shared.model.BookingInformation;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.activity.SubscriptionDetails;
import se.acrend.sj2cal.content.ProviderHelper;
import se.acrend.sj2cal.content.ProviderTypes;
import se.acrend.sj2cal.intent.Intents;
import se.acrend.sj2cal.model.DbModel;
import se.acrend.sj2cal.model.DbModel.TimeModel;
import se.acrend.sj2cal.parser.xml.BookingInfoParser;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.util.DateUtil;
import se.acrend.sj2cal.util.HttpUtil;
import se.acrend.sj2cal.util.TimeSource;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

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
  private BookingInfoParser bookingInfoParser;
  @Inject
  private ConnectivityManager connectivityManager;
  @Inject
  private AlarmManager alarmManager;
  @Inject
  private Context context;
  @Inject
  private NotificationManager notificationManager;

  private int retryCount = 0;

  private static final int MAX_RETRY_COUNT = 5;

  public void deleteBooking(final Uri data) {
    Log.d(TAG, "Delete booking");

    try {
      DbModel model = providerHelper.findTicket(data);

      callUnRegister(model);

      providerHelper.delete(data);
    } catch (Exception e) {
      Log.e(TAG, "Error in delete.", e);
    }
  }

  private void callUnRegister(final DbModel info) {
    Log.d(TAG, "UnRegister booking");

    try {

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.REGISTRATION_PATH);

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("action", "unregister"));
      nameValuePairs.add(new BasicNameValuePair("code", info.getCode()));

      HttpResponse response = communicationHelper.callServer(post, nameValuePairs);

      if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
        info.setRegistered(false);
      }

      providerHelper.update(info);

      response.getEntity().consumeContent();
    } catch (Exception e) {
      Log.e(TAG, "Error in callUnRegister.", e);
    }
  }

  public boolean callRegistration(final DbModel model) {
    Log.d(TAG, "Registrera bokning");

    try {
      String registrationId = prefsHelper.getRegistrationId();

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.REGISTRATION_PATH);

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("action", "register"));
      nameValuePairs.add(new BasicNameValuePair("code", model.getCode()));
      nameValuePairs.add(new BasicNameValuePair("trainNo", model.getTrain()));
      nameValuePairs.add(new BasicNameValuePair("from", model.getFrom()));
      nameValuePairs.add(new BasicNameValuePair("to", model.getTo()));
      nameValuePairs.add(new BasicNameValuePair("date", DateUtil.formatDate(model.getDeparture().getOriginal())));
      nameValuePairs.add(new BasicNameValuePair("registrationId", registrationId));

      HttpResponse response = communicationHelper.callServer(post, nameValuePairs);

      BookingInformation information = bookingInfoParser.parser(response.getEntity().getContent());

      if (information.getReturnCode() == ReturnCode.Success) {
        model.setRegistered(true);
        model.setDepartureTrack(information.getDepartureTrack());
        model.setArrivalTrack(information.getArrivalTrack());

        TimeModel departure = model.getDeparture();
        departure.setActual(information.getActualDeparture());
        departure.setEstimated(information.getEstimatedDeparture());
        departure.setGuessed(information.getGuessedDeparture());

        TimeModel arrival = model.getArrival();
        arrival.setEstimated(information.getActualArrival());
        arrival.setEstimated(information.getEstimatedArrival());
        arrival.setGuessed(information.getGuessedArrival());

        retryCount = 0;
      } else {
        Log.w(TAG, "Tog emot fel från server: " + information.getErrorCode());
        model.setRegistered(false);
        ErrorCode errorCode = information.getErrorCode();
        if (errorCode == ErrorCode.UpdateNotificationSubscription) {
          Notification notification = new Notification();
          notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS
              | Notification.DEFAULT_VIBRATE;
          notification.icon = R.drawable.sj2cal_bw;
          notification.when = System.currentTimeMillis();
          notification.flags = Notification.FLAG_AUTO_CANCEL;
          notification.tickerText = "Saknar giltig prenumeration.";

          Intent notificationIntent = new Intent(context, SubscriptionDetails.class);
          notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
          PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

          notification.setLatestEventInfo(context, "Saknar giltig prenumeration.",
              "Uppdatera din prenumeration för att kunna registrera din resa.", contentIntent);

          notificationManager.notify(model.getTrain(), 1, notification);
        } else {
          // TODO Notifiera beroende på antal försök?
          scheduleNewRegistration(model);
        }
      }

      providerHelper.update(model);
    } catch (Exception e) {
      Log.e(TAG, "Error in callRegistration.", e);
      // TODO Notifiera beroende på antal försök?
      scheduleNewRegistration(model);
    }
    return model.isRegistered();
  }

  private void scheduleNewRegistration(final DbModel model) {
    if (retryCount > MAX_RETRY_COUNT) {
      Log.e(TAG, "Har uppnått max antal försök, avbryter registrering.");
      // TODO Notifiera?
    }
    retryCount++;
    Calendar fiveMinutes = Calendar.getInstance();
    fiveMinutes.add(Calendar.MINUTE, 5);

    Log.d(TAG, "Schemalägger ny registrering för id " + model.getId() + " vid " + DateUtil.formatTime(fiveMinutes));

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(Intents.REGISTER_BOOKING,
        ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId())), PendingIntent.FLAG_ONE_SHOT);
    alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutes.getTimeInMillis(), pendingIntent);
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }
}
