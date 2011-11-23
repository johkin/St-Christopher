package se.acrend.sj2cal.module;

import se.acrend.sj2cal.calendar.CalendarHelper;
import se.acrend.sj2cal.content.ProviderHelper;
import se.acrend.sj2cal.parser.ConfirmationParser;
import se.acrend.sj2cal.parser.MessageHandler;
import se.acrend.sj2cal.parser.SmsTicketParser;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.service.ServerCommunicationHelper;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Binder;
import com.google.inject.Module;

public class Sj2CalModule implements Module {

  @Override
  public void configure(final Binder binder) {
    binder.bind(ProviderHelper.class);
    binder.bind(CalendarHelper.class);
    binder.bind(PrefsHelper.class);

    binder.bind(ConfirmationParser.class);
    binder.bind(SmsTicketParser.class);
    binder.bind(MessageHandler.class);

    binder.bind(ServerCommunicationHelper.class);

    binder.bind(GoogleAnalyticsTracker.class).toInstance(GoogleAnalyticsTracker.getInstance());
  }

}
