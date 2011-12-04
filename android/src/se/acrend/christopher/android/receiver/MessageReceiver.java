package se.acrend.christopher.android.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.android.parser.MessageHandler;
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

    tracker.trackEvent("Ticket", "Received", "Test", 0);
    tracker.dispatch();
  }
}
