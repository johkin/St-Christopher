package se.acrend.christopher.server.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.control.TrafikverketController;
import se.acrend.christopher.server.control.impl.SubscriptionControllerImpl;
import se.acrend.christopher.server.dao.BookingDao;
import se.acrend.christopher.server.dao.TrainStopDao;
import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.service.impl.TrafficServiceImpl.TrainStopField.Type;
import se.acrend.christopher.server.util.Constants;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.AbstractResponse;
import se.acrend.christopher.shared.model.BookingInformation;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
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
  @Qualifier("TrafikVerketJsonController")
  private TrafikverketController trafikVerketController;
  @Autowired
  private SubscriptionControllerImpl subscriptionController;
  @Autowired
  private UserService userService;
  @Autowired
  private DatastoreService datastore;
  @Autowired
  private TrainStopDao trainStopDao;
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
    Transaction transaction = null;
    try {

      BookingInformation result = new BookingInformation();

      Entity subscription = subscriptionController.findSubscription();

      subscription.setProperty("registrationId", registrationId);

      if (!isValid(subscription)) {
        result.setReturnCode(ReturnCode.Failure);
        result.setErrorCode(ErrorCode.UpdateNotificationSubscription);
        return result;
      }

      List<Entity> stops = trainStopDao.findByTrainNo(trainNo, cal);

      if (stops.isEmpty()) {
        TrainInfo info = null;
        try {
          info = trafikVerketController.getTagInfo(trainNo, cal);
        } catch (TemporaryException e) {
          log.error("Kunde inte registrera bokning för tåg {}, datum {}.", trainNo, DateUtil.formatDate(cal));
          result.setReturnCode(ReturnCode.Failure);
          result.setErrorCode(ErrorCode.ParseError);
          return result;
        } catch (PermanentException e) {
          log.error("Kunde inte registrera bokning för tåg {}, datum {}.", trainNo, DateUtil.formatDate(cal));
          result.setReturnCode(ReturnCode.Failure);
          result.setErrorCode(ErrorCode.TrainNotFound);
          return result;
        }

        log.debug("Hämtat information för tåg {}", trainNo);

        stops = convert(info.getStations(), cal, trainNo);
        for (Entity stop : stops) {
          transaction = datastore.beginTransaction();
          datastore.put(stop);
          transaction.commit();
        }
      }

      User user = userService.getCurrentUser();
      Entity booking = bookingDao.findByCodeUser(code, user.getEmail());

      Entity departureStop = findStopByName(from, stops);
      Entity arrivalStop = findStopByName(to, stops);

      if (departureStop != null) {
        result.setDepartureTrack((String) departureStop.getProperty("departureTrack"));
        result.setActualDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("actualDeparture")));
        result.setEstimatedDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("estimatedDeparture")));
        result.setGuessedDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("guessedDeparture")));
      }
      if (arrivalStop != null) {
        result.setArrivalTrack((String) arrivalStop.getProperty("arrivalTrack"));
        result.setActualArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("actualDeparture")));
        result.setEstimatedArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("estimatedDeparture")));
        result.setGuessedArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("guessedArrival")));
      }

      if (booking == null) {
        booking = new Entity(DataConstants.KIND_BOOKING, subscription.getKey());
      }
      if (departureStop != null) {
        log.debug("Bokning från {}", departureStop.getProperty("stationName"));
        booking.setProperty("departure", departureStop.getKey());
      }
      if (arrivalStop != null) {
        log.debug("Bokning till {}", arrivalStop.getProperty("stationName"));
        booking.setProperty("arrival", arrivalStop.getKey());
      }
      booking.setProperty("userEmail", user.getEmail());
      booking.setProperty("date", new Date(cal.getTimeInMillis()));
      booking.setProperty("trainNo", trainNo);
      booking.setProperty("code", code);
      booking.setProperty("registrationId", registrationId);

      transaction = datastore.beginTransaction();

      datastore.put(booking);

      transaction.commit();

      subscriptionController.update(subscription);

      return result;
    } catch (Exception e) {
      log.error("Fel vid registrering!", e);
      if (transaction != null) {
        if (transaction.isActive()) {
          transaction.rollback();
        }
      }
      return null;
    }
  }

  private boolean isValid(final Entity subscription) {

    Calendar now = DateUtil.createCalendar();
    Calendar expireDate = DateUtil.createCalendar();
    expireDate.setTime((Date) subscription.getProperty("notificationExpireDate"));

    if (expireDate.after(now) || expireDate.equals(now)) {
      return true;
    }

    long graceNotification = (Long) subscription.getProperty("notificationCount");
    if (graceNotification > 0) {
      graceNotification--;
      subscription.setProperty("notificationCount", graceNotification);
      return true;
    }

    return false;
  }

  public boolean unRegisterBooking(final String code) {

    log.debug("Biljettkod: {}", code);
    UserService userService = UserServiceFactory.getUserService();
    Transaction transaction = null;
    try {
      User user = userService.getCurrentUser();
      Entity booking = bookingDao.findByCodeUser(code, user.getEmail());

      if (booking != null) {
        transaction = datastore.beginTransaction();

        datastore.delete(booking.getKey());

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
    Transaction transaction = null;
    try {

      Calendar cal = DateUtil.createCalendar();
      cal.setTime(date);

      TrainInfo tagInfo = trafikVerketController.getTagInfo(trainNo, cal);

      log.debug("Hämtat tåginfo för tåg {}, {}", trainNo, DateUtil.formatDate(cal));

      List<Entity> stops = trainStopDao.findByTrainNo(trainNo, cal);

      if (stops.isEmpty()) {
        log.debug("Kunde inte hitta några stop för tåg {}, {}", trainNo, DateUtil.formatDate(cal));
        return;
      }

      List<TaskOptions> tasks = new ArrayList<TaskOptions>();
      List<Entity> newStops = convert(tagInfo.getStations(), cal, trainNo);

      for (Entity oldStop : stops) {
        for (Entity newStop : newStops) {
          if (oldStop.getProperty("stationName").equals(newStop.getProperty("stationName"))) {
            List<TrainStopField> modifiedFields = getModifiedFields(oldStop, newStop);
            if (!modifiedFields.isEmpty()) {
              log.debug("Uppdaterad station {} för tåg {}", newStop.getProperty("stationName"), trainNo);
              transaction = datastore.beginTransaction();
              updateProperties(oldStop, newStop);
              datastore.put(oldStop);
              transaction.commit();

              log.debug("Nyckel för ändrad station {} ", oldStop.getKey());

              List<Entity> bookings = bookingDao.findByDeparture(oldStop.getKey());

              log.debug("Antal bokningar med avresa för tåg {}, station {}: {} ",
                  new Object[] { newStop.getProperty("stationName"), trainNo, bookings.size() });

              tasks.addAll(createSendMessage(oldStop, bookings, modifiedFields, true));

              bookings = bookingDao.findByArrival(oldStop.getKey());
              log.debug("Antal bokningar med ankomst för tåg {}, station {}: {} ",
                  new Object[] { newStop.getProperty("stationName"), trainNo, bookings.size() });
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
      if (transaction != null) {
        if (transaction.isActive()) {
          transaction.rollback();
        }
      }
    }
  }

  private List<TaskOptions> createSendMessage(final Entity currentStop, final List<Entity> bookings,
      final List<TrainStopField> modifiedFields, final boolean departure) {
    List<TaskOptions> tasks = new ArrayList<TaskOptions>();
    for (Entity booking : bookings) {
      if (booking.hasProperty("registrationId")) {
        TaskOptions options = TaskOptions.Builder.withParam("registrationId",
            (String) booking.getProperty("registrationId"));
        options.param("trainNo", (String) currentStop.getProperty("trainNo"));
        options.param("code", (String) booking.getProperty("code"));
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

  private void updateProperties(final Entity oldStop, final Entity newStop) {
    oldStop.setPropertiesFrom(newStop);
    // oldStop.setActualArrival(newStop.getActualArrival());
    // oldStop.setActualDeparture(newStop.getActualDeparture());
    // oldStop.setEstimatedArrival(newStop.getEstimatedArrival());
    // oldStop.setEstimatedDeparture(newStop.getEstimatedDeparture());
    // oldStop.setGuessedArrival(newStop.getGuessedArrival());
    // oldStop.setGuessedDeparture(newStop.getGuessedDeparture());
    // oldStop.setOriginalArrival(newStop.getOriginalArrival());
    // oldStop.setOriginalDeparture(newStop.getOriginalDeparture());
    // oldStop.setArrivalStatus(newStop.getArrivalStatus());
    // oldStop.setDepartureStatus(newStop.getDepartureStatus());
    // oldStop.setInfo(newStop.getInfo());
    // oldStop.setTrack(newStop.getTrack());
  }

  private List<TrainStopField> getModifiedFields(final Entity oldStop, final Entity newStop) {
    List<TrainStopField> fields = new ArrayList<TrafficServiceImpl.TrainStopField>();

    for (TrainStopField field : TrainStopField.values()) {
      if (field.isModified(oldStop, newStop)) {
        fields.add(field);
      }
    }
    if (!fields.isEmpty()) {
      log.info("Station {} har uppdaterade fält {}", oldStop.getProperty("stationName"), fields);
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

    private TrainStopField(final String fieldName, final Type type) {
      this(fieldName, fieldName, type);
    }

    private TrainStopField(final String fieldName, final String messageFieldName, final Type type) {
      this.fieldName = fieldName;
      this.messageFieldName = messageFieldName;
      this.type = type;
    }

    public String getMessageFieldName() {
      return messageFieldName;
    }

    public Type getType() {
      return type;
    }

    public String getValue(final Entity stop) {
      Object value = stop.getProperty(fieldName);
      if (value == null) {
        return null;
      }
      if (value instanceof Date) {
        return DateUtil.formatTime((Date) value);
      }
      return value.toString();
    }

    public boolean isModified(final Entity oldStop, final Entity newStop) {

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

  private List<Entity> convert(final List<StationInfo> stations, final Calendar date, final String trainNo) {
    List<Entity> list = new ArrayList<Entity>();

    for (StationInfo info : stations) {
      Entity stop = new Entity(DataConstants.KIND_TRAIN_STOP);

      if (info.getArrival() != null) {
        TimeInfo arrival = info.getArrival();
        stop.setProperty("actualArrival", DateUtil.toDate(arrival.getActual()));
        stop.setProperty("estimatedArrival", DateUtil.toDate(arrival.getEstimated()));
        stop.setProperty("originalArrival", DateUtil.toDate(arrival.getOriginal()));
        stop.setProperty("guessedArrival", DateUtil.toDate(arrival.getGuessed()));
        stop.setProperty("arrivalStatus", arrival.getStatus().name());
        stop.setProperty("arrivalTrack", arrival.getTrack());
      }
      if (info.getDeparture() != null) {
        TimeInfo departure = info.getDeparture();
        stop.setProperty("actualDeparture", DateUtil.toDate(departure.getActual()));
        stop.setProperty("estimatedDeparture", DateUtil.toDate(departure.getEstimated()));
        stop.setProperty("originalDeparture", DateUtil.toDate(departure.getOriginal()));
        stop.setProperty("guessedDeparture", DateUtil.toDate(departure.getGuessed()));
        stop.setProperty("departureStatus", departure.getStatus().name());
        stop.setProperty("departureTrack", departure.getTrack());
      }

      stop.setProperty("stationName", info.getName());
      stop.setProperty("date", DateUtil.toDate(date));
      stop.setProperty("trainNo", trainNo);

      list.add(stop);
    }

    log.debug("Converted {} stations.", list.size());

    return list;
  }

  private Entity findStopByName(final String name, final List<Entity> stops) {
    for (Entity stop : stops) {
      if (name.equals(stop.getProperty("stationName"))) {
        return stop;
      }
    }
    return null;
  }

}
