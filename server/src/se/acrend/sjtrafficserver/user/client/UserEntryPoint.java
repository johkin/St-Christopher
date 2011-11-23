package se.acrend.sjtrafficserver.user.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class UserEntryPoint implements EntryPoint {

  /**
   * This is the entry point method.
   */
  @Override
  public void onModuleLoad() {

    RootPanel.get().add(new BookingPanel());

  }
}
