package se.acrend.sj2cal.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

public class WidgetHelper {

  @Inject
  private Context context;

  public void updateWidget() {
    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

    int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, TicketWidgetProvider.class));

    Intent widgetUpdate = new Intent();
    widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);

    context.sendBroadcast(widgetUpdate);
  }
}
