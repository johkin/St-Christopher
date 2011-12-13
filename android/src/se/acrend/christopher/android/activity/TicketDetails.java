package se.acrend.christopher.android.activity;

import java.util.Calendar;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.DbModel.TimeModel;
import se.acrend.christopher.android.service.RegistrationService;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.android.util.TimeSource;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.inject.Inject;

public class TicketDetails extends RoboActivity {

  private static final String TAG = "TicketDetails";

  private static final long REGISTER_BEFORE_DEPARTURE_MILLIS = 24 * 60 * 60 * 1000;

  @InjectView(R.id.ticket_details_ticketCode)
  private TextView ticketCode;
  @InjectView(R.id.ticket_details_car)
  private TextView car;
  @InjectView(R.id.ticket_details_seat)
  private TextView seat;
  @InjectView(R.id.ticket_details_arrivalTrack)
  private TextView arrivalTrack;
  @InjectView(R.id.ticket_details_departureTrack)
  private TextView departureTrack;
  @InjectView(R.id.ticket_details_departure)
  private TextView departure;
  @InjectView(R.id.ticket_details_arrival)
  private TextView arrival;
  @InjectView(R.id.ticket_details_notify)
  private ToggleButton notify;
  @InjectView(R.id.ticket_details_showTicket)
  private Button showTicket;

  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private Context context;
  @Inject
  private ContentResolver contentResolver;
  @Inject
  private TimeSource timeSource;
  @Inject
  private RegistrationService registrationService;

  private ContentObserver contentObserver;

  private Handler handler;

  private AlertDialog ticketTextDialog;

  private Uri data;

  private DbModel model;

  private ProgressDialog dialog;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ticket_details);

    startService(new Intent(context, RegistrationService.class));

    handler = new Handler();

    final Intent intent = getIntent();
    data = intent.getData();
  }

  @Override
  protected void onResume() {
    super.onResume();

    contentObserver = new ContentObserver(handler) {

      @Override
      public void onChange(final boolean selfChange) {
        updateView();
      }

    };
    contentResolver.registerContentObserver(data, false, contentObserver);

    updateView();
  }

  @Override
  protected void onPause() {
    contentResolver.unregisterContentObserver(contentObserver);

    saveModel();
    super.onPause();
  }

  private void updateView() {
    model = providerHelper.findTicket(data);

    setTitle(model.getFrom() + " - " + model.getTo() + " Tåg " + model.getTrain());

    ticketCode.setText(model.getCode());
    if (model.getCar() != null) {
      car.setText(model.getCar());
    }
    if (model.getSeat() != null) {
      seat.setText(model.getSeat());
    }
    arrivalTrack.setText(model.getArrivalTrack());
    departureTrack.setText(model.getDepartureTrack());

    addQuickAction(departure, model.getDeparture());
    addQuickAction(arrival, model.getArrival());

    setTimeInfo(model.getDeparture(), departure);
    setTimeInfo(model.getArrival(), arrival);

    notify.setChecked(model.isNotify());

    showTicket.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Light));
        View view = getLayoutInflater().inflate(R.layout.ticket_text, null);
        builder.setView(view);
        TextView textView = (TextView) view.findViewById(R.id.ticket_text_ticketText);
        textView.setText(model.getMessage());
        ticketTextDialog = builder.setTitle("Mobilbiljett").create();
        ticketTextDialog.setOwnerActivity(TicketDetails.this);
        ticketTextDialog.setCancelable(true);
        ticketTextDialog.show();
      }
    });
  }

  private void addQuickAction(final View view, final TimeModel timeModel) {
    final QuickAction action = new QuickAction(this);
    action.addActionItem(new ActionItem(0, "Ordinarie", DateUtil.formatDateTime(timeModel.getOriginal())));
    action.addActionItem(new ActionItem(0, "Faktisk", DateUtil.formatDateTime(timeModel.getActual())));
    action.addActionItem(new ActionItem(0, "Uppskattad", DateUtil.formatDateTime(timeModel.getEstimated())));
    action.addActionItem(new ActionItem(0, "Gissad", DateUtil.formatDateTime(timeModel.getGuessed())));
    view.setClickable(true);
    view.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        action.show(v);
      }
    });
  }

  private void saveModel() {
    if (model.isNotify() != notify.isChecked()) {
      model.setNotify(notify.isChecked());

      providerHelper.update(model);
    }
  }

  private void setTimeInfo(final TimeModel timeModel, final TextView view) {
    Calendar selectedTime = null;
    if (timeModel.getActual() != null) {
      selectedTime = timeModel.getActual();
      view.setText("= " + DateUtil.formatDateTime(selectedTime));
    } else if (timeModel.getEstimated() != null) {
      selectedTime = timeModel.getEstimated();
      view.setText("~ " + DateUtil.formatDateTime(selectedTime));
    } else if (timeModel.getGuessed() != null) {
      selectedTime = timeModel.getGuessed();
      view.setText("? " + DateUtil.formatDateTime(selectedTime));
    } else {
      selectedTime = timeModel.getOriginal();
      view.setText(DateUtil.formatDateTime(selectedTime));
    }
    if (timeModel.getOriginal() == selectedTime) {
      return;
    }
    if (selectedTime.after(timeModel.getOriginal())) {
      view.setTextAppearance(context, R.style.Delayed);
    } else {
      view.setTextAppearance(context, R.style.OnTime);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.details_menu, menu);
    MenuItem registrationMenu = menu.getItem(0);
    long millis = model.getDeparture().getOriginal().getTimeInMillis() - timeSource.getCurrentMillis();
    if (model.isRegistered() || (millis > REGISTER_BEFORE_DEPARTURE_MILLIS)) {
      // registrationMenu.setEnabled(false);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    Uri data = ContentUris.withAppendedId(ProviderTypes.CONTENT_URI, model.getId());
    switch (item.getItemId()) {
    case R.id.ticket_details_menu_delete:
      // TODO ProgressDialog
      getContentResolver().unregisterContentObserver(contentObserver);
      startService(new Intent(Intents.DELETE_BOOKING, data));
      finish();
      AsyncTask<Uri, Void, Void> unregisterTask = new AsyncTask<Uri, Void, Void>() {

        @Override
        protected Void doInBackground(final Uri... params) {
          registrationService.deleteBooking(params[0]);
          return null;
        }

        @Override
        protected void onPreExecute() {
          String message = context.getResources().getString(R.string.ticket_details_unregister_wait);

          dialog = ProgressDialog.show(TicketDetails.this, "", message, true, false);
        }

        @Override
        protected void onPostExecute(final Void result) {
          if (dialog != null) {
            dialog.dismiss();
          }
          finish();
        }
      };
      unregisterTask.execute(this.data);

      return true;
    case R.id.ticket_details_menu_register:
      AsyncTask<DbModel, Void, Boolean> registerTask = new AsyncTask<DbModel, Void, Boolean>() {

        @Override
        protected Boolean doInBackground(final DbModel... params) {
          return registrationService.callRegistration(params[0]);
        }

        @Override
        protected void onPreExecute() {
          String message = context.getResources().getString(R.string.ticket_details_register_wait);

          dialog = ProgressDialog.show(TicketDetails.this, "", message, true, false);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
          if (dialog != null) {
            dialog.dismiss();
          }
        }
      };
      registerTask.execute(model);

      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }
}
