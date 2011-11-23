package se.acrend.sj2cal.content;

import java.sql.Timestamp;
import java.util.Calendar;

import se.acrend.sj2cal.R;
import se.acrend.sj2cal.activity.TicketDetails;
import se.acrend.sj2cal.util.DateUtil;
import se.acrend.sj2cal.view.DotView;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TicketAdapter extends SimpleCursorAdapter {

  private static final String TAG = "TicketAdapter";

  public static final String[] PROJECTION = new String[] { "_id", "ticketCode", "originalDeparture", "originalArrival",
      "fromStation", "toStation", "actualDeparture", "actualArrival", "estimatedDeparture", "estimatedArrival",
      "guessedDeparture", "guessedArrival", "registered" };

  private final int[] columnIds = new int[PROJECTION.length];

  private final Context context;

  public TicketAdapter(final Context context, final Cursor cursor) {
    super(context, 0, cursor, new String[] {}, new int[] {});
    this.context = context;

    for (int i = 0; i < PROJECTION.length; i++) {
      columnIds[i] = cursor.getColumnIndex(PROJECTION[i]);
    }
  }

  @Override
  public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(context);
    View itemView = inflater.inflate(R.layout.ticket_item, parent, false);

    final long id = cursor.getLong(columnIds[0]);

    itemView.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        Uri data = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, id);
        Intent intent = new Intent();
        intent.setData(data);
        intent.setClass(context, TicketDetails.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
      }
    });

    return itemView;
  }

  @Override
  public void bindView(final View v, final Context context, final Cursor cursor) {

    String code = cursor.getString(columnIds[1]);
    String departureText = cursor.getString(columnIds[2]);
    String arrivalText = cursor.getString(columnIds[3]);
    String from = cursor.getString(columnIds[4]);
    String to = cursor.getString(columnIds[5]);
    String actualDepartureText = cursor.getString(columnIds[6]);
    String actualArrivalText = cursor.getString(columnIds[7]);
    int registered = cursor.getInt(columnIds[12]);

    TextView codeText = (TextView) v.findViewById(R.id.ticket_item_ticketCode);
    codeText.setText(code);

    DotView registeredDot = (DotView) v.findViewById(R.id.ticket_item_registeredDot);
    registeredDot.setValue(registered == 1);

    TextView fromText = (TextView) v.findViewById(R.id.ticket_item_fromStation);
    fromText.setText(from);
    TextView toText = (TextView) v.findViewById(R.id.ticket_item_toStation);
    toText.setText(to);

    Calendar departure = Calendar.getInstance();
    departure.setTime(Timestamp.valueOf(departureText));
    Calendar actualDeparture = null;
    if (actualDepartureText != null) {
      actualDeparture = Calendar.getInstance();
      actualDeparture.setTime(Timestamp.valueOf(actualDepartureText));
    }

    Calendar arrival = Calendar.getInstance();
    arrival.setTime(Timestamp.valueOf(arrivalText));
    Calendar actualArrival = null;
    if (actualArrivalText != null) {
      actualArrival = Calendar.getInstance();
      actualArrival.setTime(Timestamp.valueOf(actualArrivalText));
    }

    TextView depText = (TextView) v.findViewById(R.id.ticket_item_departure);
    TextView arrText = (TextView) v.findViewById(R.id.ticket_item_arrival);

    setDateField(departure, actualDeparture, depText);
    setDateField(arrival, actualArrival, arrText);

  }

  private void setDateField(final Calendar orgTime, final Calendar actualTime, final TextView view) {
    if (actualTime != null) {
      view.setText(DateUtil.formatTime(actualTime.getTime()));
      if (actualTime.equals(orgTime)) {
        view.setTextAppearance(context, R.style.OnTime);
      } else {
        view.setTextAppearance(context, R.style.Delayed);
      }
    } else {
      view.setText(DateUtil.formatTime(orgTime.getTime()));
    }
  }
}
