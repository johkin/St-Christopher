package se.acrend.christopher.android.widget;

import java.sql.Timestamp;
import java.util.List;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.ComingTicketList;
import se.acrend.christopher.android.activity.TicketDetails;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.TimeSource;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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

  public void onUpdate(final Context context, final AppWidgetManager widgetManager, final int[] appWidgetIds) {
    if (appWidgetIds == null) {
      return;
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
            new Intent(context, ComingTicketList.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.FrameLayout01, pendingIntent);
        widgetManager.updateAppWidget(widgetId, remoteView);
      } else {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TicketDetails.class)
            .setData(ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, ticket.getId())),
            PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_before);

        remoteView.setOnClickPendingIntent(R.id.FrameLayout01, pendingIntent);

        remoteView.setTextViewText(R.id.widget_header,
            ticket.getTrain() + " " + ticket.getFrom() + " - " + ticket.getTo());
        Log.d(TAG, "trainNo: " + ticket.getTrain());
        remoteView.setTextViewText(R.id.widget_track, ticket.getDepartureTrack());
        remoteView.setTextViewText(R.id.widget_car, ticket.getCar());
        remoteView.setTextViewText(R.id.widget_seat, ticket.getSeat());
        remoteView.setTextViewText(R.id.widget_track, ticket.getDepartureTrack());
        remoteView.setTextViewText(R.id.widget_time, DateUtil.formatTime(ticket.getDeparture().getOriginal()));
        widgetManager.updateAppWidget(widgetId, remoteView);
      }
    }
  }

  public void onDeleted(final Context context, final int[] appWidgetIds) {

  }

  public void onEnabled(final Context context) {

  }
}
