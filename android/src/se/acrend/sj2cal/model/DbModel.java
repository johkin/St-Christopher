package se.acrend.sj2cal.model;

import java.util.Calendar;

public class DbModel {

  private long id;

  private String from;
  private String to;
  private final TimeModel departure = new TimeModel();
  private final TimeModel arrival = new TimeModel();
  private String car;
  private String seat;
  private String code;
  private String train;
  private String message;
  private String departureTrack;
  private String arrivalTrack;
  private String departureInfo;
  private String arrivalInfo;
  private String calendarEventUri;
  private boolean notify;
  private boolean registered;

  public TimeModel getDeparture() {
    return departure;
  }

  public TimeModel getArrival() {
    return arrival;
  }

  public boolean isNotify() {
    return notify;
  }

  public void setNotify(final boolean notify) {
    this.notify = notify;
  }

  public boolean isRegistered() {
    return registered;
  }

  public void setRegistered(final boolean registered) {
    this.registered = registered;
  }

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public String getCalendarEventUri() {
    return calendarEventUri;
  }

  public void setCalendarEventUri(final String calendarEventUri) {
    this.calendarEventUri = calendarEventUri;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(final String to) {
    this.to = to;
  }

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public String getCar() {
    return car;
  }

  public void setCar(final String car) {
    this.car = car;
  }

  public String getSeat() {
    return seat;
  }

  public void setSeat(final String seat) {
    this.seat = seat;
  }

  public String getTrain() {
    return train;
  }

  public void setTrain(final String train) {
    this.train = train;
  }

  public String getDepartureTrack() {
    return departureTrack;
  }

  public void setDepartureTrack(final String departureTrack) {
    this.departureTrack = departureTrack;
  }

  public String getArrivalTrack() {
    return arrivalTrack;
  }

  public void setArrivalTrack(final String arrivalTrack) {
    this.arrivalTrack = arrivalTrack;
  }

  public String getDepartureInfo() {
    return departureInfo;
  }

  public void setDepartureInfo(final String departureInfo) {
    this.departureInfo = departureInfo;
  }

  public String getArrivalInfo() {
    return arrivalInfo;
  }

  public void setArrivalInfo(final String arrivalInfo) {
    this.arrivalInfo = arrivalInfo;
  }

  public static class TimeModel {
    private Calendar original;
    private Calendar estimated;
    private Calendar guessed;
    private Calendar actual;

    public Calendar getOriginal() {
      return original;
    }

    public void setOriginal(final Calendar original) {
      this.original = original;
    }

    public Calendar getEstimated() {
      return estimated;
    }

    public void setEstimated(final Calendar estimated) {
      this.estimated = estimated;
    }

    public Calendar getGuessed() {
      return guessed;
    }

    public void setGuessed(final Calendar guessed) {
      this.guessed = guessed;
    }

    public Calendar getActual() {
      return actual;
    }

    public void setActual(final Calendar actual) {
      this.actual = actual;
    }
  }
}
