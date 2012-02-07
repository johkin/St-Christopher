package se.acrend.christopher.android.activity.setup;

import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegisterDeviceResult extends ActionBarActivity {

  @InjectView(R.id.register_device_result_title)
  private TextView title;
  @InjectView(R.id.register_device_result_message)
  private TextView message;

  @InjectView(R.id.register_device_result_close)
  private Button exit;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_device_result);
    setTitle(R.string.prefs_category_c2dmregistration_title);

    Intent intent = getIntent();

    title.setText(intent.getStringExtra("title"));
    message.setText(intent.getStringExtra("message"));

    exit.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        RegisterDeviceResult.this.finish();
      }
    });
  }
}
