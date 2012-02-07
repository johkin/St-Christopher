package se.acrend.christopher.android.activity.help;

import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class About extends ActionBarActivity {
  @InjectView(R.id.about_close)
  private Button exit;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);

    getActionBarHelper().setHomeButtonEnabled(true);

    setResult(RESULT_OK);

    exit.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        About.this.finish();
      }
    });
  }

}
