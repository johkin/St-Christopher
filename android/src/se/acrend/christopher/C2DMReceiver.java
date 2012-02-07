package se.acrend.christopher;

import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.shared.util.SharedConstants;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

  private static final String TAG = "C2DM";

  public C2DMReceiver() {
    super(SharedConstants.C2DM_ACCOUNT);
  }

  @Override
  public void onUnregistered(final Context context) {
    Log.d(TAG, "Unregistered");

    Intent intent = new Intent(Intents.C2DM_UNREGISTRATION_FINISHED);
    intent.putExtra("result", true);
    context.sendBroadcast(intent);
  }

  @Override
  public void onRegistered(final Context context, final String registrationId) throws java.io.IOException {
    Log.d(TAG, "Registration ID arrived: " + registrationId);

    Intent intent = new Intent(Intents.C2DM_REGISTRATION_FINISHED);
    intent.putExtra("result", true);
    intent.putExtra("registrationId", registrationId);
    context.sendBroadcast(intent);
  }

  @Override
  protected void onMessage(final Context context, final Intent intent) {
    Log.i(TAG, "Message arrived");
    // Extract the payload from the message
    Bundle extras = intent.getExtras();
    if (extras != null) {
      for (String key : extras.keySet()) {
        Log.d(TAG, key + ": " + extras.getString(key));
      }

      Intent newIntent = new Intent(Intents.BOOKING_INFORMATION);
      newIntent.putExtras(intent.getExtras());

      context.startService(newIntent);
    }
  }

  @Override
  public void onError(final Context context, final String errorId) {
    Log.e(TAG, "Error received: " + errorId);

    Intent intent = new Intent(Intents.C2DM_REGISTRATION_ERROR);
    intent.putExtra("result", false);
    intent.putExtra("errorId", errorId);
    context.sendBroadcast(intent);
  }
}