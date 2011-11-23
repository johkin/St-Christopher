package se.acrend.sjtrafficserver.user.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.BookingDao;
import se.acrend.sjtrafficserver.server.entity.BookingEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.user.client.UserService;
import se.acrend.sjtrafficserver.user.shared.Booking;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class UserServiceImpl extends RemoteServiceServlet implements UserService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private com.google.appengine.api.users.UserService userService = null;

  @Override
  public void init() throws ServletException {
    super.init();
    userService = UserServiceFactory.getUserService();
  }

  @Override
  public List<Booking> findBookings() {
    try {
      EMF.getEM();

      User user = userService.getCurrentUser();

      BookingDao bookingDao = new BookingDao();
      return convertBooking(bookingDao.findBookings(user.getEmail()));

    } catch (Exception e) {
      log.error("Fel vid hämtning!", e);
      return Collections.EMPTY_LIST;
    } finally {
      EMF.close();
    }
  }

  private List<Booking> convertBooking(final List<BookingEntity> bookings) {
    List<Booking> result = new ArrayList<Booking>();

    for (BookingEntity b : bookings) {
      Booking booking = new Booking();

      booking.setFrom(b.getDeparture().toString());
      booking.setTo(b.getArrival().toString());
      booking.setTrainNo(b.getTrainNo());
      booking.setUserEmail(b.getUserEmail());
      booking.setRegistrationId(b.getRegistrationId());

      result.add(booking);
    }

    return result;
  }

}
