package se.acrend.christopher.server.service.impl;

import java.util.Date;

import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.Entity;

public enum TrainStopField {

  ActualArrival("actualArrival", Type.Arrival), ActualDeparture("actualDeparture", Type.Departure), TrainInfo(
      "trainInfo", Type.Both, true), ArrivalInfo("arrivalInfo", Type.Arrival, true), DepartureInfo("departureInfo",
      Type.Departure, true), ArrivalTrack("arrivalTrack", Type.Arrival), DepartureTrack("departureTrack",
      Type.Departure), EstimatedArrival("estimatedArrival", Type.Arrival), EstimatedDeparture("estimatedDeparture",
      Type.Departure), GuessedArrival("guessedArrival", Type.Arrival), GuessedDeparture("guessedDeparture",
      Type.Departure), ArrivalStatus("arrivalStatus", Type.Arrival), DepartureStatus("departureStatus", Type.Departure);

  public static enum Type {
    Arrival, Departure, Both
  }

  private final String fieldName;
  private final String messageFieldName;
  private final TrainStopField.Type type;
  private final boolean markerOnly;

  private TrainStopField(final String fieldName, final TrainStopField.Type type) {
    this(fieldName, fieldName, type);
  }

  private TrainStopField(final String fieldName, final String messageFieldName, final TrainStopField.Type type) {
    this(fieldName, messageFieldName, type, false);
  }

  private TrainStopField(final String fieldName, final TrainStopField.Type type, final boolean markerOnly) {
    this(fieldName, fieldName, type, markerOnly);
  }

  private TrainStopField(final String fieldName, final String messageFieldName, final TrainStopField.Type type,
      final boolean markerOnly) {
    this.fieldName = fieldName;
    this.messageFieldName = messageFieldName;
    this.type = type;
    this.markerOnly = markerOnly;
  }

  public String getMessageFieldName() {
    return messageFieldName;
  }

  public TrainStopField.Type getType() {
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

  public String getMessageValue(final Entity stop) {
    if (markerOnly) {
      return Boolean.TRUE.toString();
    }
    return getValue(stop);
  }

  public boolean isModified(final Entity oldStop, final Entity newStop) {
    if (isDate(oldStop) || isDate(newStop)) {
      return isDateFieldModified(oldStop, newStop);
    }

    String oldValue = getValue(oldStop);
    String newValue = getValue(newStop);
    return isFieldModified(oldValue, newValue);
  }

  private boolean isDate(final Entity entity) {
    if (entity.hasProperty(fieldName) && entity.getProperty(fieldName) instanceof Date) {
      return true;
    }
    return false;
  }

  private boolean isDateFieldModified(final Entity e1, final Entity e2) {
    Date d1 = (Date) e1.getProperty(fieldName);
    Date d2 = (Date) e2.getProperty(fieldName);

    if (d1 != null && d2 != null) {
      if (Math.abs(d1.getTime() - d2.getTime()) > 60000) {
        return true;
      }
      return false;
    }
    return true;

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

  public String getFieldName() {
    return fieldName;
  }
}