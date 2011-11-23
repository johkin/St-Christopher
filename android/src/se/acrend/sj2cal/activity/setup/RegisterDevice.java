package se.acrend.sj2cal.activity.setup;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.util.Constants;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.inject.Inject;

public class RegisterDevice extends RoboActivity {

  private static final String C2DM_REGISTRATION_FINISHED = "se.acrend.sj2cal.C2DM_REGISTRATION_FINISHED";

  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;

  @InjectView(R.id.register_device_nextButton)
  private Button next;
  @InjectView(R.id.register_device_register)
  private Button register;

  private ProgressDialog dialog;

  private BroadcastReceiver receiver;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_device);
    setTitle(R.string.prefs_category_c2dmregistration_title);

    register.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {

        String message = context.getResources().getString(R.string.prefs_category_c2dmregistration_wait);

        dialog = ProgressDialog.show(RegisterDevice.this, "",
            message, true, false);

        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");

        registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));

        registrationIntent.putExtra("sender", Constants.C2DM_EMAIL);

        context.startService(registrationIntent);
      }
    });

    next.setEnabled(false);

    receiver = new BroadcastReceiver() {

      @Override
      public void onReceive(final Context context, final Intent intent) {
        if (dialog != null) {
          dialog.dismiss();
        }
        if (intent.getBooleanExtra("result", false)) {

          prefsHelper.setRegistrationId(intent.getStringExtra("registrationId"));

          next.setEnabled(true);
          context.unregisterReceiver(receiver);
        }
      }
    };
    context.registerReceiver(receiver, new IntentFilter(C2DM_REGISTRATION_FINISHED));

    next.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        setResult(RESULT_OK);
        finish();
      }
    });
  }
}
