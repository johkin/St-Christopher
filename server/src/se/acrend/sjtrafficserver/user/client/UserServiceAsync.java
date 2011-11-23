package se.acrend.sjtrafficserver.user.client;

import java.util.List;

import se.acrend.sjtrafficserver.user.shared.Booking;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserServiceAsync {

  void findBookings(AsyncCallback<List<Booking>> callback);

}
