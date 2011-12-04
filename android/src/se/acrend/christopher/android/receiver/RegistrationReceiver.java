package se.acrend.christopher.android.receiver;

import roboguice.receiver.RoboBroadcastReceiver;
import se.acrend.christopher.android.service.RegistrationService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.inject.Inject;

public class RegistrationReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "TimerRegistrator";

  @Inject
  private RegistrationService registrationService;

  @Override
  protected void handleReceive(final Context context, final Intent intent) {
    context.startService(new Intent(context, RegistrationService.class));

    Log.d(TAG, "Received Intent: " + intent);

    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
        || ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(intent.getAction())) {
      registrationService.updatePendingRegistrations();
    }
  }
}
