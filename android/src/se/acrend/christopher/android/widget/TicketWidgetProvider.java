package se.acrend.christopher.android.widget;

import java.sql.Timestamp;
import java.util.List;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.R;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.util.TimeSource;
import android.appwidget.AppWidgetManager;
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
    Log.d(TAG, "Uppdaterar widgets");
    String[] args = { new Timestamp(timeSource.getCurrentMillis()).toString() };
    List<DbModel> tickets = providerHelper.findTickets("originalArrival > ?", args);

    if (tickets.isEmpty()) {
      return;
    }

    DbModel ticket = tickets.get(0);

    for (int widgetId : appWidgetIds) {
      Log.d(TAG, "widgetId: " + widgetId);
      RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_before);
      remoteView.setTextViewText(R.id.widget_trainNo, ticket.getTrain());
      Log.d(TAG, "trainNo: " + ticket.getTrain());
      widgetManager.updateAppWidget(widgetId, remoteView);
    }
  }

  public void onDeleted(final Context context, final int[] appWidgetIds) {

  }

  public void onEnabled(final Context context) {

  }

}
