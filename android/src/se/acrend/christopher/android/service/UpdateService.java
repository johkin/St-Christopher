package se.acrend.christopher.android.service;

import java.util.Calendar;

import roboguice.service.RoboIntentService;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.TicketDetails;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.widget.TicketWidgetProvider;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.inject.Inject;

public class UpdateService extends RoboIntentService {

  private static final String TAG = "UpdateService";

  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private NotificationManager notificationManager;
  @Inject
  private Context context;

  public UpdateService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {

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
      message.append("Ny avgångstid: " + getTimeInfo(model.getDeparture()));
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
      message.append("Ny ankomsttid: " + getTimeInfo(model.getArrival()));
    }

    if (extras.containsKey("departureTrack")) {
      model.setDepartureTrack(extras.getString("departureTrack"));
      appendToMessage(message, "Nytt avgångsspår: " + extras.getString("departureTrack"));
    }
    if (extras.containsKey("arrivalTrack")) {
      model.setArrivalTrack(extras.getString("arrivalTrack"));
      appendToMessage(message, "Nytt ankomstspår: " + extras.getString("arrivalTrack"));
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

        notificationManager.notify(model.getTrain(), 1, notification);
      }
    }

    context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
        TicketWidgetProvider.class));
  }

  private String getTimeInfo(final TimeModel timeModel) {
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
    return DateUtil.formatDateTime(selectedTime);
  }

  private void appendToMessage(final StringBuilder message, final String part) {
    if (message.length() > 0) {
      message.append("\n");
    }
    message.append(part);
  }
}
