package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

public class StationInfo implements Serializable, Comparable<StationInfo> {

  private static final long serialVersionUID = 1L;
  private String name;
  private TimeInfo arrival;
  private TimeInfo departure;
  private String info;

  public StationInfo copy() {
    StationInfo copy = new StationInfo();
    copy.name = name;
    copy.info = info;
    copy.arrival = arrival.copy();
    copy.departure = departure.copy();
    return copy;
  }

  public void clear() {
    name = null;
    info = null;
    arrival = null;
    departure = null;
  }

  @Override
  public int compareTo(final StationInfo o) {
    return getSortTime().compareTo(o.getSortTime());
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(final String info) {
    this.info = info;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public TimeInfo getArrival() {
    return arrival;
  }

  public void setArrival(final TimeInfo arrival) {
    this.arrival = arrival;
  }

  public TimeInfo getDeparture() {
    return departure;
  }

  public void setDeparture(final TimeInfo departure) {
    this.departure = departure;
  }

  Calendar getSortTime() {
    if (departure != null) {
      return departure.getOriginal();
    } else {
      return arrival.getOriginal();
    }

  }

}
