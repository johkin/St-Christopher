package se.acrend.sjtrafficserver.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class AdminEntryPoint implements EntryPoint {

  /**
   * This is the entry point method.
   */
  @Override
  public void onModuleLoad() {

    TabPanel tabPanel = new TabPanel();

    tabPanel.add(new MonitoredTrainsPanel(), "Bevakade tåg");

    tabPanel.selectTab(0);

    RootPanel.get().add(tabPanel);

  }
}
