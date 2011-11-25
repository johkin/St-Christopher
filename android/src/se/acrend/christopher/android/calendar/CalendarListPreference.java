package se.acrend.christopher.android.calendar;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListAdapter;

public class CalendarListPreference extends ListPreference {

  private static final String TAG = "CalendarListPreference";

  private ListAdapter adapter;

  private long calendarId = -1;

  public CalendarListPreference(final Context context) {
    super(context);
  }

  public CalendarListPreference(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  void updateSummary() {
    try {
      calendarId = Long.parseLong(getPersistedString("-1"));
    } catch (NumberFormatException ignore) {
      Log.d(TAG, "Kunde inte läsa calendarId: " + getValue());
    }
    String summary = "<Ingen kalender vald>";
    if ((calendarId != -1) && (adapter != null)) {
      for (int pos = 0; pos < adapter.getCount(); pos++) {
        if (adapter.getItemId(pos) == calendarId) {
          summary = (String) adapter.getItem(pos);
        }
      }
    }
    setSummary(summary);
  }

  @Override
  protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
    super.onSetInitialValue(restoreValue, defaultValue);
    updateSummary();
  }

  @Override
  protected void onPrepareDialogBuilder(final Builder builder) {
    builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(final DialogInterface dialog, final int which) {
        calendarId = adapter.getItemId(which);
        CalendarListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
        dialog.dismiss();
      }
    });
    builder.setPositiveButton(null, null);
  }

  @Override
  protected void onDialogClosed(final boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    String strVal = Long.toString(calendarId);
    if (positiveResult && callChangeListener(strVal)) {
      setValue(strVal);
      updateSummary();
    }
  }

  public void setAdapter(final ListAdapter adapter) {
    this.adapter = adapter;
    updateSummary();
  }
}
