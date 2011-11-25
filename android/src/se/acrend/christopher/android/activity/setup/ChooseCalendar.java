package se.acrend.christopher.android.activity.setup;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.calendar.CalendarHelper;
import se.acrend.christopher.android.preference.PrefsHelper;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.inject.Inject;

public class ChooseCalendar extends RoboActivity {

  private static final String TAG = "ChooseCalendar";

  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private CalendarHelper calendarHelper;

  @InjectView(R.id.choose_calendar_nextButton)
  private Button next;
  @InjectView(R.id.choose_calendar_calendar)
  private Spinner calendarSpinner;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.choose_calendar);
    setTitle(R.string.prefs_category_calendar);

    final BaseAdapter adapter = calendarHelper.getCalendarList();
    calendarSpinner.setAdapter(adapter);

    next.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        int position = calendarSpinner.getSelectedItemPosition();
        final long calendarId = adapter.getItemId(position);

        prefsHelper.setCalendarId(calendarId);
        setResult(RESULT_OK);
        finish();
      }
    });
  }
}
