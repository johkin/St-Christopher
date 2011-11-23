package se.acrend.sj2cal.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.sj2cal.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TicketText extends RoboActivity {

  @InjectView(R.id.ticket_text_ticketText)
  private TextView ticketText;
  // @InjectView(R.id.ticket_text_close)
  private Button exit;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ticket_text);
  }

  @Override
  protected void onResume() {
    super.onResume();
    init();
  }

  @Override
  protected void onPause() {
    super.onPause();

  }

  private void init() {
    Intent intent = getIntent();

    String text = intent.getStringExtra("ticket");
    setTitle(R.string.ticket_text_title);

    ticketText.setText(text);

    exit.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        finish();
      }
    });
  }
}
