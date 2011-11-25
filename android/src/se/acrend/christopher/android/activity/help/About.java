package se.acrend.christopher.android.activity.help;

import se.acrend.christopher.R;
import android.app.Activity;
import android.os.Bundle;

public class About extends Activity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.about);

    setResult(RESULT_OK);
  }

}
