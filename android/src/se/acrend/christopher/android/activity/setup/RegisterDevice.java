package se.acrend.christopher.android.activity.setup;

import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarActivity;
import se.acrend.christopher.shared.util.SharedConstants;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.c2dm.C2DMessaging;
import com.google.inject.Inject;

public class RegisterDevice extends ActionBarActivity {

  @Inject
  private Context context;

  @InjectView(R.id.register_device_register)
  private Button register;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_device);
    setTitle(R.string.prefs_category_c2dmregistration_title);

    register.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {

        Intent registrationIntent = new Intent(C2DMessaging.REQUEST_REGISTRATION_INTENT);

        registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));

        registrationIntent.putExtra("sender", SharedConstants.C2DM_ACCOUNT);

        context.startService(registrationIntent);

        RegisterDevice.this.finish();

        Toast.makeText(context, R.string.prefs_category_c2dmregistration_wait, Toast.LENGTH_LONG).show();
      }
    });
  }
}
