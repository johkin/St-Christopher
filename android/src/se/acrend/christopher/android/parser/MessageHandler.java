package se.acrend.christopher.android.parser;

import java.util.ArrayList;
import java.util.List;

import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.TicketTabActivity;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.content.ProviderTypes;
import se.acrend.christopher.android.intent.Intents;
import se.acrend.christopher.android.model.DbModel;
import se.acrend.christopher.android.model.MessageWrapper;
import se.acrend.christopher.android.widget.TicketWidgetProvider;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.inject.Inject;

public class MessageHandler {

  private static final String TAG = "MessageHandler";

  @Inject
  private ProviderHelper providerHelper;
  @Inject
  private Context context;
  @Inject
  private NotificationManager notificationManager;

  private final List<MessageParser> parsers;

  public MessageHandler() {
    parsers = new ArrayList<MessageParser>();
    parsers.add(new SmsTicketParser());
  }

  public boolean handleMessage(final String sender, final String msgBody) {

    MessageParser parser = getMessageParser(sender, msgBody);
    if (parser == null) {
      return false;
    }
    long ticketId = -1;
    MessageWrapper wrapper = parser.parse(msgBody);

    DbModel model = providerHelper.findByCode(wrapper.getCode());
    if (model == null) {
      model = new DbModel();
    }

    model.setCode(wrapper.getCode());
    model.setFrom(wrapper.getFrom());
    model.setMessage(wrapper.getMessage());
    model.setTo(wrapper.getTo());
    model.getArrival().setOriginal(wrapper.getArrival());
    model.setCar(wrapper.getCar());
    model.getDeparture().setOriginal(wrapper.getDeparture());
    model.setSeat(wrapper.getSeat());
    model.setTrain(wrapper.getTrain());
    model.setRegistered(false);
    model.setNotify(true);

    if (model.getId() > 0) {
      providerHelper.update(model);
      ticketId = model.getId();
    } else {
      ticketId = providerHelper.addEvent(model);
    }

    Intent intent = new Intent(Intents.PREPARE_REGISTRATION, ContentUris.withAppendedId(ProviderTypes.CONTENT_URI,
        ticketId));
    context.startService(intent);

    Log.d(TAG, "Start service for ticket: " + wrapper.getCode());

    context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setClass(context,
        TicketWidgetProvider.class));

    notifyMessage();

    return true;
  }

  private void notifyMessage() {
    Notification notification = new Notification();
    notification.icon = R.drawable.ic_launcher_logo_bw;
    notification.when = System.currentTimeMillis();
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    notification.tickerText = "Nya biljetter mottagna.";
    Intent notificationIntent = new Intent(context, TicketTabActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, "Lagt till biljett.",
        "Har lagt till nya biljetter. Kontrollera informationen.", contentIntent);

    notificationManager.notify(1, notification);
  }

  private MessageParser getMessageParser(final String sender, final String message) {
    for (MessageParser parser : parsers) {
      if (parser.supports(sender, message)) {
        return parser;
      }
    }
    return null;
  }
}
