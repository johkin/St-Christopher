package se.acrend.sj2cal.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.parser.MessageHandler;
import se.acrend.sj2cal.preference.PrefsHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Inject;

public class SmsReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "SmsReceiver";

  @Inject
  private MessageHandler messageHandler;
  @Inject
  private GoogleAnalyticsTracker tracker;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);
    if (!PrefsHelper.isProcessIncommingMessages(context)) {
      return;
    }

    Bundle bundle = intent.getExtras();

    Object messages[] = (Object[]) bundle.get("pdus");
    String msgBody = "";
    for (Object message2 : messages) {
      SmsMessage message = SmsMessage.createFromPdu((byte[]) message2);
      msgBody += message.getDisplayMessageBody();
    }

    boolean success = messageHandler.handleMessage(msgBody);

    if (success) {
      String analyticsCode = context.getString(R.string.analytics_code);
      tracker.startNewSession(analyticsCode, context);
      tracker.trackEvent("Ticket", "Received", "SMS", 0);
      tracker.stopSession();
      tracker.dispatch();
    }

    if (PrefsHelper.isDeleteProcessedMessages(context) && success) {
      abortBroadcast();
    }
  }
}
