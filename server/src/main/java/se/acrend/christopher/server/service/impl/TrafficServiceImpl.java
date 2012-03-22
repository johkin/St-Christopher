package se.acrend.christopher.server.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import se.acrend.christopher.server.dao.StationMappingDao;
import se.acrend.christopher.server.dao.TrainDao;
import se.acrend.christopher.server.dao.TrainStopDao;
import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.util.Constants;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.server.util.QueueUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.BookingInformation;
import se.acrend.christopher.shared.model.ErrorCode;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.memcache.MemcacheService;
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
  private TrainDao trainDao;
  @Autowired
  private BookingDao bookingDao;
  @Autowired
  private StationMappingDao stationMappingDao;
  @Autowired
  private ConfigurationServiceImpl configurationService;
  @Autowired
  private MailService mailService;
  @Autowired
  private MemcacheService memcacheService;

  public BookingInformation registerBooking(final String code, final String trainNo, final Date date,
      final String from, final String to, final String registrationId) {

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

      User user = userService.getCurrentUser();
      Entity booking = bookingDao.findByCodeUser(code, user.getEmail());

      if (booking == null) {
        booking = new Entity(DataConstants.KIND_BOOKING, subscription.getKey());
      }
      booking.setProperty("departureName", from);
      booking.setProperty("arrivalName", to);

      List<Entity> stops = trainStopDao.findByTrainNo(trainNo, cal);

      if (stops.isEmpty()) {
        Entity train = trainDao.findByTrainNo(trainNo, cal);
        if (train == null) {
          train = new Entity(DataConstants.KIND_TRAIN);
          train.setProperty("trainNo", trainNo);
          train.setProperty("date", date);
          train.setProperty("loaded", false);

          transaction = datastore.beginTransaction();

          datastore.put(train);

          transaction.commit();
        }
      } else {
        String mappedFrom = mapStationName(from);
        String mappedto = mapStationName(to);

        Entity departureStop = findStopByName(mappedFrom, stops);
        Entity arrivalStop = findStopByName(mappedto, stops);

        if (departureStop != null) {
          result.setDepartureTrack((String) departureStop.getProperty("departureTrack"));
          result.setOriginalDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("originalDeparture")));
          result.setActualDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("actualDeparture")));
          result.setEstimatedDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("estimatedDeparture")));
          result.setGuessedDeparture(DateUtil.toCalendar((Date) departureStop.getProperty("guessedDeparture")));
          result.setDepartureInfo((String) departureStop.getProperty("departureInfo"));
        } else {
          log.warn("Kunde inte hitta station {} för tåg {}", from, trainNo);
          sendMissingStationEmail(trainNo, from, stops);
        }
        if (arrivalStop != null) {
          result.setArrivalTrack((String) arrivalStop.getProperty("arrivalTrack"));
          result.setOriginalArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("originalArrival")));
          result.setActualArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("actualArrival")));
          result.setEstimatedArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("estimatedArrival")));
          result.setGuessedArrival(DateUtil.toCalendar((Date) arrivalStop.getProperty("guessedArrival")));
          result.setArrivalInfo((String) departureStop.getProperty("arrivalInfo"));
        } else {
          log.warn("Kunde inte hitta station {} för tåg {}", to, trainNo);
          sendMissingStationEmail(trainNo, to, stops);
        }

        if (departureStop != null) {
          log.debug("Bokning från {}", departureStop.getProperty("stationName"));
          booking.setProperty("departure", departureStop.getKey());
        }
        if (arrivalStop != null) {
          log.debug("Bokning till {}", arrivalStop.getProperty("stationName"));
          booking.setProperty("arrival", arrivalStop.getKey());
        }
        result.setInformationCode("UpdateBooking");
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

      result.setReturnCode(ReturnCode.Success);

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

  private void sendMissingStationEmail(final String trainNo, final String stationName, final List<Entity> stops) {

    MailService.Message message = new MailService.Message();
    message.setSender("johan.kindgren@acrend.se");

    message.setSubject("St Christopher: Saknad station: " + stationName);

    StringBuilder body = new StringBuilder();
    body.append("<html><body>");
    body.append("Hej!<br/>");
    body.append("Vilken station ska ").append(stationName).append(" ersättas med?<br/>");
    body.append("<ul>");
    String hostUrl = "http://localhost:8888";
    String environment = System.getProperty("com.google.appengine.runtime.environment");
    if ("Production".equalsIgnoreCase(environment)) {
      String applicationId = System.getProperty("com.google.appengine.application.id");
      String version = System.getProperty("com.google.appengine.application.version");
      hostUrl = "http://" + version + "." + applicationId + ".appspot.com/";
    }
    try {
      hostUrl += "dispatch/mapStationName?from=" + URLEncoder.encode(stationName, Constants.ENCODING) + "&to=";
    } catch (UnsupportedEncodingException e) {
      log.error("Could not encode stationName {}", stationName, e);
      hostUrl += "dispatch/mapStationName?from=" + stationName + "&to=";
    }

    log.debug("Url för stations-mappning: " + hostUrl);

    for (Entity stop : stops) {
      String newName = (String) stop.getProperty("stationName");
      body.append("<li><a href='").append(hostUrl).append(newName)
          .append("'>").append(newName).append("</a></li>");
    }
    body.append("</ul>");
    body.append("</body></html>");

    message.setHtmlBody(body.toString());

    try {
      mailService.sendToAdmins(message);
    } catch (IOException e) {
      log.error("Kunde inte skicka mail om saknad station", e);
      throw new PermanentException("Kunde inte skicka mail om saknad station", e);
    }
  }

  private String mapStationName(final String sjName) {
    Entity mappedStation = stationMappingDao.findBySj(sjName);
    if (mappedStation == null) {
      return sjName;
    }
    String newName = (String) mappedStation.getProperty("trafikverket");
    log.debug("Har konverterat stationsnamn från {} till {}", sjName, newName);
    return newName;
  }

  private boolean isValid(final Entity subscription) {

    Calendar now = DateUtil.createCalendar();
    Calendar expireDate = DateUtil.createCalendar();
    expireDate.setTime((Date) subscription.getProperty("notificationExpireDate"));

    if (expireDate.after(now) || expireDate.equals(now)) {
      return true;
    }

    long notificationCount = (Long) subscription.getProperty("notificationCount");
    if (notificationCount > 0) {
      notificationCount--;
      subscription.setProperty("notificationCount", notificationCount);
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
      List<Entity> newStops = convert(tagInfo.getStations(), cal, trainNo, null);

      for (Entity oldStop : stops) {
        for (Entity newStop : newStops) {
          if (oldStop.getProperty("stationName").equals(newStop.getProperty("stationName"))) {
            List<TrainStopField> modifiedFields = getModifiedFields(oldStop, newStop);
            if (!modifiedFields.isEmpty()) {
              log.debug("Uppdaterad station {} för tåg {}", newStop.getProperty("stationName"), trainNo);
              transaction = datastore.beginTransaction();
              updateProperties(oldStop, newStop, modifiedFields);
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
      Queue sendQueue = QueueFactory.getQueue(QueueUtil.SEND_MESSAGE_QUEUE_NAME);
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

        TrainStopField.Type type = null;
        if (departure) {
          type = TrainStopField.Type.Departure;
        } else {
          type = TrainStopField.Type.Arrival;
        }

        for (TrainStopField field : modifiedFields) {
          if ((field.getType() == TrainStopField.Type.Both) || (field.getType() == type)) {
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

  private void updateProperties(final Entity oldStop, final Entity newStop, final List<TrainStopField> fields) {
    for (TrainStopField field : fields) {
      oldStop.setProperty(field.getFieldName(), newStop.getProperty(field.getFieldName()));
    }
  }

  private List<TrainStopField> getModifiedFields(final Entity oldStop, final Entity newStop) {
    List<TrainStopField> fields = new ArrayList<TrainStopField>();

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

  private List<Entity> convert(final List<StationInfo> stations, final Calendar date, final String trainNo,
      final Key trainKey) {
    List<Entity> list = new ArrayList<Entity>();

    for (StationInfo info : stations) {
      Entity stop = new Entity(DataConstants.KIND_TRAIN_STOP, trainKey);

      if (info.getArrival() != null) {
        TimeInfo arrival = info.getArrival();
        stop.setProperty("actualArrival", DateUtil.toDate(arrival.getActual()));
        stop.setProperty("estimatedArrival", DateUtil.toDate(arrival.getEstimated()));
        stop.setProperty("originalArrival", DateUtil.toDate(arrival.getOriginal()));
        stop.setProperty("guessedArrival", DateUtil.toDate(arrival.getGuessed()));
        stop.setProperty("arrivalStatus", arrival.getStatus().name());
        stop.setProperty("arrivalTrack", arrival.getTrack());
        stop.setProperty("arrivalInfo", arrival.getInfo());
      }
      if (info.getDeparture() != null) {
        TimeInfo departure = info.getDeparture();
        stop.setProperty("actualDeparture", DateUtil.toDate(departure.getActual()));
        stop.setProperty("estimatedDeparture", DateUtil.toDate(departure.getEstimated()));
        stop.setProperty("originalDeparture", DateUtil.toDate(departure.getOriginal()));
        stop.setProperty("guessedDeparture", DateUtil.toDate(departure.getGuessed()));
        stop.setProperty("departureStatus", departure.getStatus().name());
        stop.setProperty("departureTrack", departure.getTrack());
        stop.setProperty("departureInfo", departure.getInfo());
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
      String stationName = (String) stop.getProperty("stationName");
      if (name.equals(stationName)) {
        return stop;
      }
    }
    return null;
  }

  /**
   * Hämta information om tåg och spara
   * 
   * @param trainNo
   * @param time
   */
  public void loadTrain(final String trainNo, final Calendar cal) {
    log.debug("Trying to load train {}.", trainNo);

    List<Entity> stops = trainStopDao.findByTrainNo(trainNo, cal);

    if (stops.isEmpty()) {
      resetCache(trainNo, cal);

      Entity train = trainDao.findByTrainNo(trainNo, cal);
      TrainInfo info = null;
      try {
        info = trafikVerketController.getTagInfo(trainNo, cal);
      } catch (TemporaryException e) {
        log.error("Kunde inte registrera bokning för tåg {}, datum {}.", trainNo, DateUtil.formatDate(cal));
        throw e;
      } catch (PermanentException e) {
        log.error("Kunde inte registrera bokning för tåg {}, datum {}.", trainNo, DateUtil.formatDate(cal));
        throw e;
      }

      log.debug("Hämtat information för tåg {}", trainNo);

      stops = convert(info.getStations(), cal, trainNo, train.getKey());
      for (Entity stop : stops) {
        Transaction transaction = datastore.beginTransaction();
        datastore.put(stop);
        transaction.commit();
      }

      List<Entity> bookings = bookingDao.findByTrainNoAndDate(trainNo, cal);
      for (Entity booking : bookings) {
        log.debug("Updating booking {}.", booking.getProperty("code"));

        String departureName = (String) booking.getProperty("departureName");
        Entity departureStop = findStopByName(departureName, stops);
        if (departureStop == null) {
          departureName = mapStationName(departureName);
          departureStop = findStopByName(departureName, stops);
        }
        if (departureStop == null) {
          sendMissingStationEmail(trainNo, (String) booking.getProperty("departureName"), stops);
        } else {
          booking.setProperty("departure", departureStop.getKey());
        }

        String arrivalName = (String) booking.getProperty("arrivalName");
        Entity arrivalStop = findStopByName(arrivalName, stops);
        if (arrivalStop == null) {
          arrivalName = mapStationName(arrivalName);
          arrivalStop = findStopByName(arrivalName, stops);
        }
        if (arrivalStop == null) {
          sendMissingStationEmail(trainNo, (String) booking.getProperty("arrivalName"), stops);
        } else {
          booking.setProperty("arrival", arrivalStop.getKey());
        }

        Transaction transaction = datastore.beginTransaction();
        datastore.put(booking);
        transaction.commit();
      }

      Transaction transaction = datastore.beginTransaction();
      train.setProperty("loaded", true);
      datastore.put(train);
      transaction.commit();

      sendUpdateMessage(bookings);
    } else {
      log.debug("Train {} already loaded.", trainNo);
    }
  }

  void resetCache(final String trainNo, final Calendar cal) {

    String key = DateUtil.formatDate(cal) + "-" + trainNo;

    log.debug("Clearing cache for trainNo {}, date {}", trainNo, DateUtil.formatDate(cal));

    if (memcacheService.contains(key)) {
      memcacheService.delete(key);
    }
  }

  public void createStationNameMapping(final String sjName, final String trafikVerketName) {

    log.debug("Adding mapping from {} to {}", sjName, trafikVerketName);

    Entity mapping = stationMappingDao.findBySj(sjName);
    if (mapping == null) {
      mapping = new Entity(DataConstants.KIND_STATION_MAPPING);
      mapping.setProperty("sj", sjName);
    }
    mapping.setProperty("trafikverket", trafikVerketName);

    Transaction transaction = datastore.beginTransaction();

    datastore.put(mapping);

    transaction.commit();

    List<Entity> trainStops = trainStopDao.findByStationName(trafikVerketName);

    List<Entity> bookingsToUpdate = updateBookingsWithTrainStopKey(sjName, trainStops);

    sendUpdateMessage(bookingsToUpdate);
  }

  private void sendUpdateMessage(final List<Entity> bookingsToUpdate) {

    List<TaskOptions> tasks = new ArrayList<TaskOptions>();
    for (Entity booking : bookingsToUpdate) {
      TaskOptions options = TaskOptions.Builder.withParam("registrationId",
          (String) booking.getProperty("registrationId"));
      options.param("trainNo", (String) booking.getProperty("trainNo"));
      options.param("code", (String) booking.getProperty("code"));
      options.param("bookingKey", KeyFactory.keyToString(booking.getKey()));
      options.param("updateFromProxy", "true");

      tasks.add(options);
    }

    Queue sendQueue = QueueFactory.getQueue(QueueUtil.SEND_MESSAGE_QUEUE_NAME);
    sendQueue.add(tasks);
  }

  private List<Entity> updateBookingsWithTrainStopKey(final String sjName, final List<Entity> trainStops) {
    Transaction transaction;
    List<Entity> bookingsToUpdate = new ArrayList<Entity>();
    for (Entity stop : trainStops) {
      String trainNo = (String) stop.getProperty("trainNo");
      Date date = (Date) stop.getProperty("date");

      log.debug("Updating bookings for train {} at {}", trainNo, date);

      List<Entity> bookings = bookingDao.findByDepartureName(sjName, trainNo, date);
      for (Entity booking : bookings) {
        booking.setProperty("departure", stop.getKey());

        log.debug("Updating booking with code {}", booking.getProperty("code"));

        transaction = datastore.beginTransaction();
        datastore.put(booking);
        transaction.commit();

        bookingsToUpdate.add(booking);
      }
      bookings = bookingDao.findByArrivalName(sjName, trainNo, date);
      for (Entity booking : bookings) {
        booking.setProperty("arrival", stop.getKey());

        log.debug("Updating booking with code {}", booking.getProperty("code"));

        transaction = datastore.beginTransaction();
        datastore.put(booking);
        transaction.commit();
        bookingsToUpdate.add(booking);
      }
    }
    return bookingsToUpdate;
  }

}
