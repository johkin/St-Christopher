package se.acrend.christopher.android.widget;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.TicketDetails;
import se.acrend.christopher.android.activity.TicketTabActivity;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.TimeSource;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.inject.Inject;

public class TicketWidgetProvider extends RoboBroadcastReceiver {

  private static final String TAG = "TicketWidgetProvider";

  @Inject
  private TimeSource timeSource;
  @Inject
  private ProviderHelper providerHelper;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Broadcast för TicketWidget: " + intent);

    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
    int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
    if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
      onUpdate(context, widgetManager, ids);
    } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(intent.getAction())) {
      onDeleted(context, ids);
    } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction())) {
      onEnabled(context);
    }
  }

  public void onUpdate(final Context context, final AppWidgetManager widgetManager, int[] appWidgetIds) {
    if (appWidgetIds == null) {
      appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
    }
    Log.d(TAG, "Uppdaterar widgets");
    String[] args = { new Timestamp(timeSource.getCurrentMillis()).toString() };
    List<DbModel> tickets = providerHelper.findTickets("originalArrival > ?", args);

    DbModel ticket = null;
    if (!tickets.isEmpty()) {
      ticket = tickets.get(0);
    }

    for (int widgetId : appWidgetIds) {
      Log.d(TAG, "widgetId: " + widgetId);
      if (ticket == null) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_empty);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, TicketTabActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.widgetlayout, pendingIntent);
        widgetManager.updateAppWidget(widgetId, remoteView);
      } else {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TicketDetails.class)
            .setData(ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, ticket.getId())),
            PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar endViewLimit = (Calendar) ticket.getArrival().getOriginal().clone();
        endViewLimit.add(Calendar.MINUTE, -16);

        RemoteViews remoteView = null;
        if (ticket.getDeparture().getActual() == null) {
          remoteView = createBeforeView(context, ticket);
        } else if (timeSource.getCurrentCalender().before(endViewLimit)) {
          remoteView = createDuringView(context, ticket);
        } else {
          remoteView = createEndView(context, ticket);
        }

        remoteView.setOnClickPendingIntent(R.id.widgetlayout, pendingIntent);
        widgetManager.updateAppWidget(widgetId, remoteView);
      }
    }
  }

  RemoteViews createBeforeView(final Context context, final DbModel ticket) {
    RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_before);

    remoteView.setTextViewText(R.id.widget_header, ticket.getTrain() + " " + ticket.getFrom() + " - " + ticket.getTo());
    Log.d(TAG, "trainNo: " + ticket.getTrain());
    remoteView.setTextViewText(R.id.widget_track, ticket.getDepartureTrack());
    remoteView.setTextViewText(R.id.widget_car, ticket.getCar());
    remoteView.setTextViewText(R.id.widget_seat, ticket.getSeat());

    TimeModel timeModel = ticket.getDeparture();

    formatTime(remoteView, timeModel, R.id.widget_time);
    return remoteView;
  }

  RemoteViews createDuringView(final Context context, final DbModel ticket) {
    RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_during);

    remoteView.setTextViewText(R.id.widget_header, ticket.getTrain() + " " + ticket.getTo());
    Log.d(TAG, "trainNo: " + ticket.getTrain());
    remoteView.setTextViewText(R.id.widget_car, ticket.getCar());
    remoteView.setTextViewText(R.id.widget_seat, ticket.getSeat());
    remoteView.setTextViewText(R.id.widget_code, ticket.getCode());

    TimeModel timeModel = ticket.getArrival();

    formatTime(remoteView, timeModel, R.id.widget_time);
    return remoteView;
  }

  RemoteViews createEndView(final Context context, final DbModel ticket) {
    RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_end);

    remoteView.setTextViewText(R.id.widget_header, ticket.getTrain() + " " + ticket.getTo());
    Log.d(TAG, "trainNo: " + ticket.getTrain());
    remoteView.setTextViewText(R.id.widget_track, ticket.getArrivalTrack());
    remoteView.setTextViewText(R.id.widget_car, ticket.getCar());
    remoteView.setTextViewText(R.id.widget_seat, ticket.getSeat());

    TimeModel timeModel = ticket.getArrival();

    formatTime(remoteView, timeModel, R.id.widget_time);
    return remoteView;
  }

  void formatTime(final RemoteViews remoteView, final TimeModel timeModel, final int viewId) {
    Calendar selectedTime = null;
    String prefix = "";
    if (timeModel.getActual() != null) {
      selectedTime = timeModel.getActual();
      prefix = "= ";
    } else if (timeModel.getEstimated() != null) {
      selectedTime = timeModel.getEstimated();
      prefix = "~ ";
    } else if (timeModel.getGuessed() != null) {
      selectedTime = timeModel.getGuessed();
      prefix = "? ";
    } else {
      selectedTime = timeModel.getOriginal();
      prefix = "";
    }

    if (selectedTime != timeModel.getOriginal()) {
      if (selectedTime.after(timeModel.getOriginal())) {
        remoteView.setTextColor(viewId, Color.RED);
      } else {
        remoteView.setTextColor(viewId, Color.GREEN);
      }
    }
    remoteView.setTextViewText(viewId, prefix + DateUtil.formatShortDateTime(selectedTime));
  }

  public void onDeleted(final Context context, final int[] appWidgetIds) {

  }

  public void onEnabled(final Context context) {

  }
}
