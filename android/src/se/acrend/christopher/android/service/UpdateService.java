package se.acrend.christopher.android.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import roboguice.service.RoboIntentService;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.TicketDetails;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.HttpUtil;
import se.acrend.christopher.android.widget.TicketWidgetProvider;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TimeInfo.Status;
import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.christopher.shared.parser.ParserFactory;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class UpdateService extends RoboIntentService {

  private static final String TAG = "UpdateService";

  private static final int MAX_RETRY_COUNT = 5;

  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private NotificationManager notificationManager;
  @Inject
  private AlarmManager alarmManager;
  @Inject
  private Context context;
  @Inject
  private ServerCommunicationHelper communicationHelper;

  public UpdateService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {

    if (intent != null) {
      if (Intents.BOOKING_INFORMATION.equals(intent.getAction())) {
        updateFromC2dm(intent);
      } else if (Intents.UPDATE_BOOKING.equals(intent.getAction())) {

        DbModel model = providerHelper.findTicket(intent.getData());

        updateFromProxy(model, intent.getIntExtra("retryCount", 1));

        providerHelper.update(model);

        context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
            TicketWidgetProvider.class));
      }
    }
  }

  void updateFromC2dm(final Intent intent) {
    Bundle extras = intent.getExtras();

    StringBuilder message = new StringBuilder();

    String code = extras.getString("code");
    DbModel model = providerHelper.findByCode(code);

    TimeModel departure = model.getDeparture();
    boolean changedDepartureTime = false;
    if (extras.containsKey("actualDeparture")) {
      departure.setActual(DateUtil.parseDateTime(extras.getString("actualDeparture")));
      changedDepartureTime = true;
    }
    if (extras.containsKey("estimatedDeparture")) {
      departure.setEstimated(DateUtil.parseDateTime(extras.getString("estimatedDeparture")));
      changedDepartureTime = true;
    }
    if (extras.containsKey("guessedDeparture")) {
      departure.setGuessed(DateUtil.parseDateTime(extras.getString("guessedDeparture")));
      changedDepartureTime = true;
    }
    if (changedDepartureTime) {
      message.append("Ny avgångstid: " + DateUtil.formatTime(getMostSignificantTime(model.getDeparture())));
    }

    TimeModel arrival = model.getArrival();
    boolean changedArrivalTime = false;
    if (extras.containsKey("actualArrival")) {
      arrival.setActual(DateUtil.parseDateTime(extras.getString("actualArrival")));
      changedArrivalTime = true;
    }
    if (extras.containsKey("estimatedArrival")) {
      arrival.setEstimated(DateUtil.parseDateTime(extras.getString("estimatedArrival")));
      changedArrivalTime = true;
    }
    if (extras.containsKey("guessedArrival")) {
      arrival.setGuessed(DateUtil.parseDateTime(extras.getString("guessedArrival")));
      changedArrivalTime = true;
    }
    if (changedArrivalTime) {
      Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      updateIntent.setClass(context, TicketWidgetProvider.class);
      PendingIntent pending = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      Calendar orgTime = getMostSignificantTime(arrival);
      Calendar alarmTime = (Calendar) orgTime.clone();
      alarmTime.add(Calendar.MINUTE, -15);

      alarmManager.set(AlarmManager.RTC, alarmTime.getTimeInMillis(), pending);

      message.append("Ny ankomsttid: " + DateUtil.formatTime(orgTime));
    }

    if (extras.containsKey("departureTrack")) {
      model.setDepartureTrack(extras.getString("departureTrack"));
      appendToMessage(message, "Nytt avgångsspår: " + extras.getString("departureTrack"));
    }
    if (extras.containsKey("arrivalTrack")) {
      model.setArrivalTrack(extras.getString("arrivalTrack"));
      appendToMessage(message, "Nytt ankomstspår: " + extras.getString("arrivalTrack"));
    }

    Status departureStatus = null;
    if (extras.containsKey("departureStatus")) {
      departureStatus = Status.valueOf(extras.getString("departureStatus"));
    }

    Status arrivalStatus = null;
    if (extras.containsKey("arrivalStatus")) {
      arrivalStatus = Status.valueOf(extras.getString("arrivalStatus"));
    }
    if (arrivalStatus == Status.Cancelled) {
      model.getArrival().setCancelled(true);
    } else {
      model.getArrival().setCancelled(false);

    }
    if (departureStatus == Status.Cancelled) {
      model.getDeparture().setCancelled(true);
    } else {
      model.getDeparture().setCancelled(false);
    }

    if (extras.containsKey("info")) {
      Intent updateIntent = new Intent(Intents.UPDATE_BOOKING, ContentUris.withAppendedId(ProviderTypes.CONTENT_URI,
          model.getId()));
      startService(updateIntent);
    }

    boolean cancelled = model.getDeparture().isCancelled() || model.getArrival().isCancelled();
    if (cancelled) {
      message = new StringBuilder();
      if (model.getDeparture().isCancelled()) {
        message.append("Avgång inställd!");
      }
      if (model.getArrival().isCancelled()) {
        message.append("Ankomst inställd!");
      }
    }

    providerHelper.update(model);

    if (model.isNotify()) {
      if (message.length() > 0) {
        Notification notification = new Notification();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        notification.icon = R.drawable.ic_launcher_logo_bw;
        notification.when = System.currentTimeMillis();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.tickerText = "Tåg " + model.getTrain() + " uppdaterat";

        Uri data = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId());
        Intent notificationIntent = new Intent(context, TicketDetails.class);
        notificationIntent.setData(data);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, "Tåg " + model.getTrain(), message.toString(), contentIntent);

        notificationManager.notify(model.getTrain(), (int) model.getId(), notification);
      }
    }

    context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
        TicketWidgetProvider.class));
  }

  public void updateFromProxy(final DbModel model, final int retryCount) {
    if (retryCount > 5) {
      // TODO notifiera om problem att hämta information.
      return;
    }

    try {
      HttpPost post = communicationHelper.createPostRequest(HttpUtil.PROXY_PATH + "/");

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("trainNo", model.getTrain()));
      nameValuePairs.add(new BasicNameValuePair("date", DateUtil.formatDate(model.getDeparture().getOriginal())));

      HttpResponse response = communicationHelper.callServer(post, nameValuePairs);

      Gson gson = ParserFactory.createParser();
      TrainInfo information = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), TrainInfo.class);

      updateModel(model, information);

      // Todo Notifiera om ny info

    } catch (IOException e) {
      Log.e(TAG, "Kunde inte uppdatera bokning", e);
      // TODO Notifiera om fel vid uppdatering
      Intent updateIntent = new Intent(Intents.UPDATE_BOOKING, ContentUris.withAppendedId(ProviderTypes.CONTENT_URI,
          model.getId()));
      updateIntent.putExtra("retryCount", retryCount + 1);

      PendingIntent pending = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      Calendar fiveMinutes = DateUtil.createCalendar();
      fiveMinutes.add(Calendar.MINUTE, 5);

      alarmManager.set(AlarmManager.RTC, fiveMinutes.getTimeInMillis(), pending);
    }

  }

  void updateModel(final DbModel model, final TrainInfo information) {
    String departureName = model.getFrom();
    String arrivalName = model.getTo();

    StationInfo departureStation = getStation(departureName, information.getStations());
    StationInfo arrivalStation = getStation(arrivalName, information.getStations());
    if (departureStation != null) {
      TimeInfo departure = departureStation.getDeparture();
      model.setDepartureTrack(departure.getTrack());
      TimeModel modelDeparture = model.getDeparture();
      modelDeparture.setActual(departure.getActual());
      modelDeparture.setCancelled(departure.getStatus() == Status.Cancelled);
      modelDeparture.setEstimated(departure.getEstimated());
      modelDeparture.setGuessed(departure.getGuessed());
      modelDeparture.setInfo(departure.getInfo());
      modelDeparture.setOriginal(departure.getOriginal());
    }
    if (arrivalStation != null) {
      TimeInfo arrival = arrivalStation.getArrival();
      model.setArrivalTrack(arrival.getTrack());
      TimeModel modelArrival = model.getArrival();
      modelArrival.setActual(arrival.getActual());
      modelArrival.setCancelled(arrival.getStatus() == Status.Cancelled);
      modelArrival.setEstimated(arrival.getEstimated());
      modelArrival.setGuessed(arrival.getGuessed());
      modelArrival.setInfo(arrival.getInfo());
      modelArrival.setOriginal(arrival.getOriginal());
    }
  }

  private StationInfo getStation(final String name, final List<StationInfo> stations) {
    for (StationInfo info : stations) {
      if (name.equals(info.getName())) {
        return info;
      }
    }
    return null;
  }

  private Calendar getMostSignificantTime(final TimeModel timeModel) {
    Calendar selectedTime = null;
    if (timeModel.getActual() != null) {
      selectedTime = timeModel.getActual();
    } else if (timeModel.getEstimated() != null) {
      selectedTime = timeModel.getEstimated();
    } else if (timeModel.getGuessed() != null) {
      selectedTime = timeModel.getGuessed();
    } else {
      selectedTime = timeModel.getOriginal();
    }
    return selectedTime;
  }

  private void appendToMessage(final StringBuilder message, final String part) {
    if (message.length() > 0) {
      message.append("\n");
    }
    message.append(part);
  }
}
