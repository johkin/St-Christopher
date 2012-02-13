package se.acrend.christopher.android.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.setup.RegisterDeviceResult;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.service.RegistrationService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.inject.Inject;

public class C2DmResultReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "C2DmResultReceiver";

  @Inject
  private NotificationManager notificationManager;

  @Inject
  private PrefsHelper prefsHelper;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    context.startService(new Intent(context, RegistrationService.class));

    Log.d(TAG, "Received Intent: " + intent);

    String title = null;
    String message = null;

    if (intent.getBooleanExtra("result", false)) {

      prefsHelper.setRegistrationId(intent.getStringExtra("registrationId"));

      prefsHelper.setProcessIncommingMessages(true);

      title = context.getString(R.string.prefs_category_c2dmregistration_success_title);
      message = context.getString(R.string.prefs_category_c2dmregistration_success_message);

    } else {

      prefsHelper.setRegistrationId(null);

      prefsHelper.setProcessIncommingMessages(false);

      String errorId = intent.getStringExtra("errorId");

      title = context.getString(R.string.prefs_category_c2dmregistration_error_title);
      message = context.getString(R.string.prefs_category_c2dmregistration_error_serverMessage);

      if ("SERVICE_NOT_AVAILABLE".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_SERVICE_NOT_AVAILABLE);
      } else if ("ACCOUNT_MISSING".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_ACCOUNT_MISSING);
      } else if ("AUTHENTICATION_FAILED".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_AUTHENTICATION_FAILED);
      } else if ("TOO_MANY_REGISTRATIONS".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_TOO_MANY_REGISTRATIONS);
      } else if ("INVALID_SENDER".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_INVALID_SENDER);
      } else if ("PHONE_REGISTRATION_ERROR".equals(errorId)) {
        message += context.getString(R.string.prefs_category_c2dmregistration_error_PHONE_REGISTRATION_ERROR);
      }
    }

    Notification notification = new Notification();
    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
    notification.icon = R.drawable.ic_launcher_logo_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = title;

    Intent resultIntent = new Intent(context, RegisterDeviceResult.class);
    resultIntent.putExtra("title", title);
    resultIntent.putExtra("message", message);

    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);

    notification.setLatestEventInfo(context, title,
        message, contentIntent);

    notificationManager.notify("registrationResult", 1, notification);

  }
}
