package se.acrend.christopher.android.module;

import se.acrend.christopher.android.calendar.CalendarHelper;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.parser.ConfirmationParser;
import se.acrend.christopher.android.parser.MessageHandler;
import se.acrend.christopher.android.parser.SmsTicketParser;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.service.ServerCommunicationHelper;

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
