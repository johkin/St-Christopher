package se.acrend.sjtrafficserver.admin.client;

import com.google.gwt.user.client.ui.Composite;

public class BookingPanel extends Composite {

  // private final TrafficServiceAsync trafficService;
  //
  // private final TextBox bookingNo;
  // private final TextBox trainNo;
  // private final TextBox fromStation;
  // private final TextBox toStation;
  // private final DatePicker date;
  // private final Button sendButton;
  // private final Grid formLayout;
  //
  // private final Button checkTrafficButton;
  //
  // private final Label errorLabel;
  //
  // private final VerticalPanel bookingPanel;

  public BookingPanel() {

    // trafficService = GWT.create(TrafficService.class);
    //
    // sendButton = new Button("Send");
    // bookingNo = new TextBox();
    // trainNo = new TextBox();
    // fromStation = new TextBox();
    // toStation = new TextBox();
    // date = new DatePicker();
    // date.setValue(new Date());
    //
    // errorLabel = new Label();
    //
    // formLayout = new Grid(2, 6);
    // formLayout.setWidget(0, 0, new Label("Bokningsnummer"));
    // formLayout.setWidget(1, 0, trainNo);
    // formLayout.setWidget(0, 1, new Label("Tågnr"));
    // formLayout.setWidget(1, 1, trainNo);
    // formLayout.setWidget(0, 2, new Label("Från station"));
    // formLayout.setWidget(1, 2, fromStation);
    // formLayout.setWidget(0, 3, new Label("Till station"));
    // formLayout.setWidget(1, 3, toStation);
    // formLayout.setWidget(0, 4, new Label("Datum"));
    // formLayout.setWidget(1, 4, date);
    // formLayout.setWidget(1, 5, sendButton);
    //
    // bookingPanel = new VerticalPanel();
    //
    // checkTrafficButton = new Button();
    // checkTrafficButton.setText("Kontrollera trafik");
    // checkTrafficButton.addClickHandler(new ClickHandler() {
    //
    // @Override
    // public void onClick(final ClickEvent event) {
    // checkTrafficButton.setEnabled(false);
    // trafficService.checkTraffic(new AsyncCallback<Boolean>() {
    //
    // @Override
    // public void onFailure(final Throwable caught) {
    // errorLabel.setText("Kunde inte hämta lista av bokningar: " +
    // caught.getMessage());
    // checkTrafficButton.setEnabled(true);
    // }
    //
    // @Override
    // public void onSuccess(final Boolean result) {
    // errorLabel.setText("");
    // checkTrafficButton.setEnabled(true);
    // }
    // });
    // }
    // });
    //
    // bookingPanel.add(errorLabel);
    // bookingPanel.add(formLayout);
    // bookingPanel.add(checkTrafficButton);
    //
    // trainNo.setFocus(true);
    // trainNo.selectAll();
    //
    // // Create a handler for the sendButton and nameField
    // class MyHandler implements ClickHandler, KeyUpHandler {
    // /**
    // * Fired when the user clicks on the sendButton.
    // */
    // @Override
    // public void onClick(final ClickEvent event) {
    // registerBooking();
    // }
    //
    // /**
    // * Fired when the user types in the nameField.
    // */
    // @Override
    // public void onKeyUp(final KeyUpEvent event) {
    // if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
    // registerBooking();
    // }
    // }
    //
    // /**
    // * Send the name from the nameField to the server and wait for a response.
    // */
    // private void registerBooking() {
    // // First, we validate the input.
    // errorLabel.setText("");
    // String textToServer = trainNo.getText();
    // if (!FieldVerifier.isValidTrain(textToServer)) {
    // errorLabel.setText("Ange tågnr");
    // return;
    // }
    //
    // // Then, we send the input to the server.
    // sendButton.setEnabled(false);
    // trafficService.registerBooking(bookingNo.getText(), trainNo.getText(),
    // date.getValue(), fromStation.getValue(),
    // toStation.getValue(), null, new AsyncCallback<BookingInformation>() {
    // @Override
    // public void onFailure(final Throwable caught) {
    // // Show the RPC error message to the user
    // errorLabel.setText("Remote Procedure Call - Failure");
    // sendButton.setEnabled(true);
    // }
    //
    // @Override
    // public void onSuccess(final BookingInformation result) {
    // errorLabel.setText("Result: " + result);
    //
    // sendButton.setEnabled(true);
    // }
    // });
    // }
    // }
    //
    // // Add a handler to send the name to the server
    // MyHandler handler = new MyHandler();
    // sendButton.addClickHandler(handler);
    // trainNo.addKeyUpHandler(handler);
    //
    // initWidget(bookingPanel);
  }
}
