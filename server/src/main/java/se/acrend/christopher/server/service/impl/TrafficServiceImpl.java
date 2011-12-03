package se.acrend.christopher.server.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.control.impl.SubscriptionControllerImpl;
import se.acrend.christopher.server.control.impl.TrafikVerketControllerImpl;
import se.acrend.christopher.server.dao.BookingDao;
import se.acrend.christopher.server.dao.TrainStopDao;
import se.acrend.christopher.server.entity.BookingEntity;
import se.acrend.christopher.server.entity.SubscriptionEntity;
import se.acrend.christopher.server.entity.TrainStopEntity;
import se.acrend.christopher.server.service.impl.TrafficServiceImpl.TrainStopField.Type;
import se.acrend.christopher.server.util.Constants;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.AbstractResponse;
import se.acrend.christopher.shared.model.BookingInformation;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Component
public class TrafficServiceImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private TrafikVerketControllerImpl trafikVerketController;
  @Autowired
  private SubscriptionControllerImpl subscriptionController;
  @Autowired
  private UserService userService;
  @Autowired
  private EntityManagerFactory entityManagerFactory;
  @Autowired
  private TrainStopDao trainDao;
  @Autowired
  private BookingDao bookingDao;

  public AbstractResponse registerBooking(final String code, final String trainNo, final Date date, final String from,
      final String to, final String registrationId) {

    Calendar cal = DateUtil.createCalendar();
    cal.setTime(date);

    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    log.debug("Tåg: {}, datum: {}, från: {}, till: {}", new String[] { trainNo, DateUtil.formatDate(cal), from, to });
    EntityTransaction transaction = null;
    try {
      transaction = entityManagerFactory.createEntityManager().getTransaction();

      BookingInformation result = new BookingInformation();

      transaction.begin();

      SubscriptionEntity subscription = subscriptionController.findSubscription();

      transaction.commit();
      if (!isValid(subscription)) {
        result.setReturnCode(ReturnCode.Failure);
        result.setErrorCode(ErrorCode.UpdateNotificationSubscription);
        return result;
      }
      TrainInfo info = null;
      try {
        info = trafikVerketController.getTagInfo(trainNo, cal);
      } catch (TemporaryException e) {
        log.error("Kunde inte registrera bokning för tåg {}, datum {}.", trainNo, DateUtil.formatDate(cal));
        result.setReturnCode(ReturnCode.Failure);
        result.setErrorCode(ErrorCode.ParseError);
        return result;
      }

      log.debug("Parsat xml: {}", info);

      List<TrainStopEntity> stops = trainDao.findByTrainNo(trainNo, cal);
      if (stops.isEmpty()) {
        stops = convert(info.getStations(), cal, trainNo);
        for (TrainStopEntity stop : stops) {
          transaction.begin();
          trainDao.create(stop);
          transaction.commit();
        }
      }

      User user = userService.getCurrentUser();

      BookingEntity booking = bookingDao.findByCodeUser(code, user.getEmail());

      TrainStopEntity departureStop = findStopByName(from, stops);
      TrainStopEntity arrivalStop = findStopByName(to, stops);

      if (departureStop != null) {
        result.setDepartureTrack(departureStop.getTrack());
        result.setActualDeparture(DateUtil.toCalendar(departureStop.getActualDeparture()));
        result.setEstimatedDeparture(DateUtil.toCalendar(departureStop.getEstimatedDeparture()));
        result.setGuessedDeparture(DateUtil.toCalendar(departureStop.getGuessedDeparture()));
      }
      if (arrivalStop != null) {
        result.setArrivalTrack(arrivalStop.getTrack());
        result.setActualArrival(DateUtil.toCalendar(arrivalStop.getActualDeparture()));
        result.setEstimatedArrival(DateUtil.toCalendar(arrivalStop.getEstimatedDeparture()));
        result.setGuessedArrival(DateUtil.toCalendar(arrivalStop.getGuessedArrival()));
      }

      if (booking == null) {
        booking = new BookingEntity();
        if (departureStop != null) {
          log.debug("Bokning från {}", departureStop.getStationName());
          booking.setDeparture(departureStop.getKey());
        }
        if (arrivalStop != null) {
          log.debug("Bokning till {}", arrivalStop.getStationName());
          booking.setArrival(arrivalStop.getKey());
        }
        booking.setUserEmail(user.getEmail());
        booking.setDate(new Date(cal.getTimeInMillis()));
        booking.setTrainNo(trainNo);
        booking.setCode(code);
        booking.setRegistrationId(registrationId);

        transaction.begin();

        bookingDao.create(booking);

        transaction.commit();
      }

      transaction.begin();

      subscriptionController.update(subscription);

      transaction.commit();

      return result;
    } catch (Exception e) {
      log.error("Fel vid registrering!", e);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      return null;
    }
  }

  private boolean isValid(final SubscriptionEntity subscription) {

    Calendar now = DateUtil.createCalendar();
    Calendar expireDate = DateUtil.createCalendar();
    expireDate.setTime(subscription.getNotificationExpireDate());

    if (expireDate.after(now) || expireDate.equals(now)) {
      return true;
    }

    int graceNotification = subscription.getNotificationCount();
    if (graceNotification > 0) {
      graceNotification--;
      subscription.setNotificationCount(graceNotification);
      return true;
    }

    return false;
  }

  public boolean unRegisterBooking(final String code) {

    log.debug("Biljettkod: {}", code);
    UserService userService = UserServiceFactory.getUserService();
    EntityTransaction transaction = null;
    try {
      transaction = entityManagerFactory.createEntityManager().getTransaction();

      User user = userService.getCurrentUser();

      BookingEntity booking = bookingDao.findByCodeUser(code, user.getEmail());

      if (booking != null) {
        transaction.begin();

        bookingDao.delete(booking);

        transaction.commit();
      }
      return true;
    } catch (Exception e) {
      log.error("Fel vid borttag!", e);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      return false;
    }
  }

  public void checkTrainChanges(final String trainNo, final Date date) {
    EntityTransaction transaction = entityManagerFactory.createEntityManager().getTransaction();
    try {

      Calendar cal = DateUtil.createCalendar();
      cal.setTime(date);

      TrainInfo tagInfo = trafikVerketController.getTagInfo(trainNo, cal);

      log.debug("Hämtat tåginfo för tåg {}, {}", trainNo, DateUtil.formatDate(cal));

      List<TrainStopEntity> stops = trainDao.findByTrainNo(trainNo, cal);
      if (stops.isEmpty()) {
        log.debug("Kunde inte hitta några stop för tåg {}, {}", trainNo, DateUtil.formatDate(cal));
        return;
      }

      List<TaskOptions> tasks = new ArrayList<TaskOptions>();
      List<TrainStopEntity> newStops = convert(tagInfo.getStations(), cal, trainNo);

      for (TrainStopEntity oldStop : stops) {
        for (TrainStopEntity newStop : newStops) {
          if (oldStop.getStationName().equals(newStop.getStationName())) {
            List<TrainStopField> modifiedFields = getModifiedFields(oldStop, newStop);
            if (!modifiedFields.isEmpty()) {
              log.debug("Uppdaterad station {} för tåg {}", newStop.getStationName(), trainNo);
              transaction.begin();
              updateProperties(oldStop, newStop);
              trainDao.update(oldStop);
              transaction.commit();

              log.debug("Nyckel för ändrad station {} ", oldStop.getKey());

              List<BookingEntity> bookings = bookingDao.findByDeparture(oldStop.getKey());
              log.debug("Antal bokningar med avresa för tåg {}, station {}: {} ",
                  new Object[] { newStop.getStationName(), trainNo, bookings.size() });
              tasks.addAll(createSendMessage(oldStop, bookings, modifiedFields, true));
              bookings = bookingDao.findByArrival(oldStop.getKey());
              log.debug("Antal bokningar med ankomst för tåg {}, station {}: {} ",
                  new Object[] { newStop.getStationName(), trainNo, bookings.size() });
              tasks.addAll(createSendMessage(oldStop, bookings, modifiedFields, false));
            }
            break;
          }
        }
      }
      Queue sendQueue = QueueFactory.getQueue(Constants.SEND_MESSAGE_QUEUE_NAME);
      sendQueue.add(tasks);
    } catch (Exception e) {
      log.error("Fel vid hämtning!", e);
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  private List<TaskOptions> createSendMessage(final TrainStopEntity currentStop, final List<BookingEntity> bookings,
      final List<TrainStopField> modifiedFields, final boolean departure) {
    List<TaskOptions> tasks = new ArrayList<TaskOptions>();
    for (BookingEntity booking : bookings) {
      if (booking.getRegistrationId() != null) {
        TaskOptions options = TaskOptions.Builder.withParam("registrationId", booking.getRegistrationId());
        options.param("trainNo", currentStop.getTrainNo());
        options.param("code", booking.getCode());
        options.param("bookingKey", KeyFactory.keyToString(booking.getKey()));

        Type type = null;
        if (departure) {
          type = Type.Departure;
        } else {
          type = Type.Arrival;
        }

        for (TrainStopField field : modifiedFields) {
          if ((field.getType() == Type.Both) || (field.getType() == type)) {
            String value = field.getValue(currentStop);
            if (value != null) {
              options.param(field.getMessageFieldName(), field.getValue(currentStop));
            }
          }
        }

        tasks.add(options);
      }
    }
    return tasks;
  }

  private void updateProperties(final TrainStopEntity oldStop, final TrainStopEntity newStop) {
    oldStop.setActualArrival(newStop.getActualArrival());
    oldStop.setActualDeparture(newStop.getActualDeparture());
    oldStop.setEstimatedArrival(newStop.getEstimatedArrival());
    oldStop.setEstimatedDeparture(newStop.getEstimatedDeparture());
    oldStop.setGuessedArrival(newStop.getGuessedArrival());
    oldStop.setGuessedDeparture(newStop.getGuessedDeparture());
    oldStop.setOriginalArrival(newStop.getOriginalArrival());
    oldStop.setOriginalDeparture(newStop.getOriginalDeparture());
    oldStop.setArrivalStatus(newStop.getArrivalStatus());
    oldStop.setDepartureStatus(newStop.getDepartureStatus());
    oldStop.setInfo(newStop.getInfo());
    oldStop.setTrack(newStop.getTrack());
  }

  private List<TrainStopField> getModifiedFields(final TrainStopEntity oldStop, final TrainStopEntity newStop) {
    List<TrainStopField> fields = new ArrayList<TrafficServiceImpl.TrainStopField>();

    for (TrainStopField field : TrainStopField.values()) {
      if (field.isModified(oldStop, newStop)) {
        fields.add(field);
      }
    }
    if (!fields.isEmpty()) {
      log.info("Station {} har uppdaterade fält {}", oldStop.getStationName(), fields);
    }

    return fields;
  }

  public static enum TrainStopField {

    ActualArrival("actualArrival", Type.Arrival), ActualDeparture("actualDeparture", Type.Departure), TrainInfo(
        "trainInfo", Type.Both), Info("info", Type.Both), ArrivalTrack("track", "arrivalTrack", Type.Arrival), DepartureTrack(
        "track", "departureTrack", Type.Departure), EstimatedArrival("estimatedArrival", Type.Arrival), EstimatedDeparture(
        "estimatedDeparture", Type.Departure), GuessedArrival("guessedArrival", Type.Arrival), GuessedDeparture(
        "guessedDeparture", Type.Departure), ArrivalStatus("arrivalStatus", Type.Arrival), DepartureStatus(
        "departureStatus", Type.Departure);

    public static enum Type {
      Arrival, Departure, Both
    }

    private final String fieldName;
    private final String messageFieldName;
    private final Type type;

    private Field field;

    private TrainStopField(final String fieldName, final Type type) {
      this(fieldName, fieldName, type);
    }

    private TrainStopField(final String fieldName, final String messageFieldName, final Type type) {
      this.fieldName = fieldName;
      this.messageFieldName = messageFieldName;
      this.type = type;
      try {
        field = TrainStopEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
      } catch (SecurityException e) {
        throw new RuntimeException("Kunde inte hämta fält: " + fieldName);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException("Felaktigt fältnamn: " + fieldName);
      }
    }

    public String getMessageFieldName() {
      return messageFieldName;
    }

    public Type getType() {
      return type;
    }

    public String getValue(final TrainStopEntity stop) {
      Object value;
      try {
        value = field.get(stop);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Felaktigt objekt: " + stop);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Kunde inte hämta fält: " + fieldName);
      }
      if (value == null) {
        return null;
      }
      if (value instanceof Date) {
        return DateUtil.formatTime((Date) value);
      }
      return value.toString();
    }

    public boolean isModified(final TrainStopEntity oldStop, final TrainStopEntity newStop) {
      String oldValue = getValue(oldStop);
      String newValue = getValue(newStop);
      return isFieldModified(oldValue, newValue);
    }

    private boolean isFieldModified(final String o1, final String o2) {
      if ((o1 == null) && (o2 == null)) {
        return false;
      }
      if (o1 != null) {
        return !o1.equals(o2);
      }
      return true;
    }
  }

  private List<TrainStopEntity> convert(final List<StationInfo> stations, final Calendar date, final String trainNo) {
    List<TrainStopEntity> list = new ArrayList<TrainStopEntity>();

    for (StationInfo info : stations) {
      TrainStopEntity stop = new TrainStopEntity();

      if (info.getArrival() != null) {
        TimeInfo arrival = info.getArrival();
        stop.setActualArrival(DateUtil.toDate(arrival.getActual()));
        stop.setEstimatedArrival(DateUtil.toDate(arrival.getEstimated()));
        stop.setOriginalArrival(DateUtil.toDate(arrival.getOriginal()));
        stop.setGuessedArrival(DateUtil.toDate(arrival.getGuessed()));
        stop.setArrivalStatus(arrival.getStatus().name());
      }
      if (info.getDeparture() != null) {
        TimeInfo departure = info.getDeparture();
        stop.setActualDeparture(DateUtil.toDate(departure.getActual()));
        stop.setEstimatedDeparture(DateUtil.toDate(departure.getEstimated()));
        stop.setOriginalDeparture(DateUtil.toDate(departure.getOriginal()));
        stop.setGuessedDeparture(DateUtil.toDate(departure.getGuessed()));
        stop.setDepartureStatus(departure.getStatus().name());
      }
      stop.setTrack(info.getTrack());
      stop.setStationName(info.getName());
      stop.setDate(DateUtil.toDate(date));
      stop.setTrainNo(trainNo);

      list.add(stop);
    }

    log.debug("Converted {} stations.", list.size());

    return list;
  }

  private TrainStopEntity findStopByName(final String name, final List<TrainStopEntity> stops) {
    for (TrainStopEntity stop : stops) {
      if (name.equals(stop.getStationName())) {
        return stop;
      }
    }
    return null;
  }

}
