package se.acrend.sj2cal.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.parser.MessageHandler;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Inject;

public class MessageReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "MessageReceiver";

  @Inject
  private MessageHandler messageHandler;
  @Inject
  private GoogleAnalyticsTracker tracker;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);

    String msgBody = intent.getStringExtra("message");

    messageHandler.handleMessage(msgBody);

    String analyticsCode = context.getString(R.string.analytics_code);
    tracker.startNewSession(analyticsCode, context);
    tracker.trackEvent("Ticket", "Received", "Test", 0);
    tracker.stopSession();
    tracker.dispatch();
  }
}
