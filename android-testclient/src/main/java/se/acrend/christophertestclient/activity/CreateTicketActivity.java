package se.acrend.christophertestclient.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.christopher.shared.util.SharedDateUtil;
import se.acrend.christophertestclient.R;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateTicketActivity extends RoboActivity {

  private static String TAG = "CreateTicketActivity";

  @InjectView(R.id.dateText)
  private TextView dateText;
  @InjectView(R.id.departureText)
  private TextView departureText;
  @InjectView(R.id.arrivalText)
  private TextView arrivalText;
  @InjectView(R.id.trainNo)
  private EditText trainNo;
  @InjectView(R.id.from)
  private Spinner from;
  @InjectView(R.id.to)
  private Spinner to;
  @InjectView(R.id.dateButton)
  private Button dateButton;
  @InjectView(R.id.departureButton)
  private Button departureButton;
  @InjectView(R.id.arrivalButton)
  private Button arrivalButton;
  @InjectView(R.id.sendButton)
  private Button send;

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM 'kl' HH:mm",
      SharedDateUtil.SWEDISH_LOCALE);

  private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", SharedDateUtil.SWEDISH_LOCALE);

  private int count = 1;

  /**
   * Called when the activity is first created.
   * 
   * @param savedInstanceState
   *          If the activity is being re-initialized after previously being
   *          shut down then this Bundle contains the data it most recently
   *          supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is
   *          null.</b>
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    String[] names = getResources().getStringArray(R.array.stationNames);

    from.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
        names));
    to.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
        names));

    Calendar now = Calendar.getInstance(SharedDateUtil.SWEDISH_TIMEZONE, SharedDateUtil.SWEDISH_LOCALE);
    dateText.setText(dateFormat.format(now.getTime()));
    departureText.setText(timeFormat.format(now.getTime()));
    arrivalText.setText(timeFormat.format(now.getTime()));

    dateButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {

        final Calendar now = Calendar.getInstance(SharedDateUtil.SWEDISH_TIMEZONE, SharedDateUtil.SWEDISH_LOCALE);
        try {
          int year = now.get(Calendar.YEAR);
          Date date = dateFormat.parse(dateText.getText().toString());
          now.setTime(date);
          now.set(Calendar.YEAR, year);
        } catch (Exception e) {
          Log.e(TAG, "Fel i datum: " + e);
        }

        DatePickerDialog dialog = new DatePickerDialog(CreateTicketActivity.this,
            new DatePickerDialog.OnDateSetListener() {

              @Override
              public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                now.set(Calendar.YEAR, year);
                now.set(Calendar.MONTH, monthOfYear);
                now.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                dateText.setText(dateFormat.format(now.getTime()));
              }

            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
      }
    });

    departureButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {

        final Calendar now = Calendar.getInstance(SharedDateUtil.SWEDISH_TIMEZONE, SharedDateUtil.SWEDISH_LOCALE);
        try {
          Date date = timeFormat.parse(departureText.getText().toString());
          now.setTime(date);
        } catch (Exception e) {
          Log.e(TAG, "Fel i datum: " + e);
        }

        TimePickerDialog dialog = new TimePickerDialog(CreateTicketActivity.this,
            new TimePickerDialog.OnTimeSetListener() {

              @Override
              public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
                now.set(Calendar.HOUR_OF_DAY, hourOfDay);
                now.set(Calendar.MINUTE, minute);

                departureText.setText(timeFormat.format(now.getTime()));
              }

            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
      }
    });

    arrivalButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {

        final Calendar now = Calendar.getInstance(SharedDateUtil.SWEDISH_TIMEZONE, SharedDateUtil.SWEDISH_LOCALE);
        try {
          Date date = timeFormat.parse(arrivalText.getText().toString());
          now.setTime(date);
        } catch (Exception e) {
          Log.e(TAG, "Fel i datum: " + e);
        }

        TimePickerDialog dialog = new TimePickerDialog(CreateTicketActivity.this,
            new TimePickerDialog.OnTimeSetListener() {

              @Override
              public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
                now.set(Calendar.HOUR_OF_DAY, hourOfDay);
                now.set(Calendar.MINUTE, minute);

                arrivalText.setText(timeFormat.format(now.getTime()));
              }

            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
      }
    });

    send.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        sendMessage();
      }
    });
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {

    super.onSaveInstanceState(outState);
  }

  // 11 jan kl 16:24
  // +'220572436'+
  // +'903765246'+
  // +'373740923'+
  // +'692092924'+
  // ÅRSKORT GULD
  // JOHAN KINDGREN
  // Avg. Norrköping C 16.24
  // Ank. Stockholm C 17.39
  // Tåg: 538
  // VU, 1 klass Kan återbetalas
  // Vagn 2, plats 30
  // Personlig biljett giltig med ID
  // Internet/Bilj.nr. SPG9352F0002
  // 010 624 472 391 895 723 215

  private void sendMessage() {
    Intent intent = new Intent("se.acrend.christopher.TEST_MESSAGE");

    StringBuilder message = new StringBuilder(dateText.getText());
    message.append("\n").append("+'220572436'+\n").append("+'903765246'+\n").append("+'373740923'+\n")
        .append("+'692092924'+\n").append("ÅRSKORTS GULD\n");

    message.append("Avg. ").append(from.getSelectedItem()).append(" ").append(departureText.getText()).append("\n");

    message.append("Ank. ").append(to.getSelectedItem()).append(" ").append(arrivalText.getText()).append("\n");
    message.append("Tåg: ").append(trainNo.getText()).append("\n");
    message.append("VU, 1 klass Kan återbetalas\n");
    message.append("Vagn 2, plats 30\n");
    message.append("Personlig biljett giltig med ID\n");
    message.append("Internet/Bilj.nr. SPG9352F").append(String.format("%04d%n", count));
    message.append("010 624 472 391 895 723 215");

    count++;

    intent.putExtra("sender", "SJ Biljett");
    intent.putExtra("message", message.toString());

    Log.d(TAG, "Biljett: " + message);

    sendBroadcast(intent, null);
  }
}
