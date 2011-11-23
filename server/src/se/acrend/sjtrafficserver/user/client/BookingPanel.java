package se.acrend.sjtrafficserver.user.client;

import java.util.List;

import se.acrend.sjtrafficserver.user.shared.Booking;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BookingPanel extends Composite {

  private final UserServiceAsync userService;

  private final Button updateBookingsButton;
  private final Grid bookings;

  private final Label errorLabel;

  private final VerticalPanel bookingPanel;

  public BookingPanel() {

    userService = GWT.create(UserService.class);

    errorLabel = new Label();

    bookings = new Grid(0, 0);

    bookingPanel = new VerticalPanel();
    updateBookingsButton = new Button();
    updateBookingsButton.setText("Uppdatera lista");
    updateBookingsButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(final ClickEvent event) {
        userService.findBookings(new AsyncCallback<List<Booking>>() {

          @Override
          public void onSuccess(final List<Booking> result) {
            bookings.resize(result.size(), 6);
            for (int i = 0; i < result.size(); i++) {
              final Booking booking = result.get(i);

              bookings.setText(i, 0, booking.getUserEmail());
              bookings.setText(i, 1, booking.getTrainNo());
              bookings.setText(i, 2, booking.getFrom());
              bookings.setText(i, 3, booking.getTo());
              final TextBox message = new TextBox();
              bookings.setWidget(i, 4, message);
              final Button sendMessageButton = new Button();
              sendMessageButton.setText("Skicka meddelande");
              if (booking.getRegistrationId() == null) {
                sendMessageButton.setEnabled(false);
              }
              bookings.setWidget(i, 5, sendMessageButton);
            }
          }

          @Override
          public void onFailure(final Throwable caught) {
            errorLabel.setText("Kunde inte hämta lista av bokningar: " + caught.getMessage());
          }
        });

      }
    });

    bookingPanel.add(errorLabel);
    bookingPanel.add(updateBookingsButton);
    bookingPanel.add(bookings);

    initWidget(bookingPanel);
  }
}
