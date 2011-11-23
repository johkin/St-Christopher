package se.acrend.sjtrafficserver.user.client;

import java.util.List;

import se.acrend.sjtrafficserver.user.shared.Booking;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {

  List<Booking> findBookings();

}
