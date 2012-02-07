package se.acrend.christopher.android.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.android.parser.MessageHandler;
import se.acrend.christopher.android.preference.PrefsHelper;
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
  @Inject
  private PrefsHelper prefsHelper;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    Log.d(TAG, "Received Intent: " + intent);
    if (!prefsHelper.isProcessIncommingMessages()) {
      return;
    }

    Bundle bundle = intent.getExtras();

    Object messages[] = (Object[]) bundle.get("pdus");
    String msgBody = "";
    String sender = null;
    for (Object message2 : messages) {
      SmsMessage message = SmsMessage.createFromPdu((byte[]) message2);
      sender = message.getDisplayOriginatingAddress();
      msgBody += message.getDisplayMessageBody();
    }

    boolean success = messageHandler.handleMessage(sender, msgBody);

    if (success) {
      tracker.trackEvent("Ticket", "Received", "SMS", 0);
      tracker.dispatch();
    }

    if (PrefsHelper.isDeleteProcessedMessages(context) && success) {
      abortBroadcast();
    }
  }
}
