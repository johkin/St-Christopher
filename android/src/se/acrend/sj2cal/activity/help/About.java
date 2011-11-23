package se.acrend.sj2cal.activity.help;

import se.acrend.sj2cal.R;
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
