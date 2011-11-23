package se.acrend.sjtrafficserver.admin.client;

import java.util.List;

import se.acrend.sjtrafficserver.admin.shared.Train;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MonitoredTrainsPanel extends Composite {

  private final AdminServiceAsync adminService;

  private final Label errorLabel;

  private Grid trainGrid;

  private final Button updateButton;

  private final VerticalPanel bookingPanel;

  public MonitoredTrainsPanel() {

    adminService = GWT.create(AdminService.class);

    errorLabel = new Label();

    trainGrid = new Grid(1, 2);
    trainGrid.setWidget(0, 0, new Label("Tågnummer"));
    trainGrid.setWidget(0, 1, new Label("Datum"));

    bookingPanel = new VerticalPanel();

    updateButton = new Button();
    updateButton.setText("Uppdatera lista");
    updateButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(final ClickEvent event) {
        updateButton.setEnabled(false);
        adminService.getMonitoredTrains(new AsyncCallback<List<Train>>() {

          @Override
          public void onSuccess(final List<Train> result) {

            updateButton.setEnabled(true);

            trainGrid = new Grid(result.size(), 2);
            trainGrid.setWidget(0, 0, new Label("Tågnummer"));
            trainGrid.setWidget(0, 1, new Label("Datum"));
            for (int i = 0; i < result.size(); i++) {
              Train t = result.get(i);
              trainGrid.setWidget(i + 1, 0, new Label(t.getTrainNo()));
              trainGrid.setWidget(i + 1, 1, new Label(t.getDate()));
            }
          }

          @Override
          public void onFailure(final Throwable caught) {
            errorLabel.setText("Kunde inte hämta bevakade tåg: " + caught.getMessage());
            updateButton.setEnabled(true);
          }
        });

      }
    });

    bookingPanel.add(errorLabel);
    bookingPanel.add(trainGrid);
    bookingPanel.add(updateButton);

    initWidget(bookingPanel);
  }
}
