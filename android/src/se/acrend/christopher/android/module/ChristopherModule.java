package se.acrend.christopher.android.module;

import se.acrend.christopher.android.application.ChristopherApp;
import se.acrend.christopher.android.content.ProviderHelper;
import se.acrend.christopher.android.parser.MessageHandler;
import se.acrend.christopher.android.parser.SmsTicketParser;
import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.service.ServerCommunicationHelper;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.inject.Binder;
import com.google.inject.Module;

public class ChristopherModule implements Module {

  @Override
  public void configure(final Binder binder) {
    binder.bind(ProviderHelper.class);
    binder.bind(PrefsHelper.class);

    binder.bind(SmsTicketParser.class);
    binder.bind(MessageHandler.class);

    binder.bind(ServerCommunicationHelper.class);

    binder.bind(GoogleAnalyticsTracker.class).toInstance(GoogleAnalyticsTracker.getInstance());

    binder.requestStaticInjection(ChristopherApp.class);
  }

}
