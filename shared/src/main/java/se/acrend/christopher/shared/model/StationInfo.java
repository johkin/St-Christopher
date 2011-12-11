package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class StationInfo implements Serializable, Comparable<StationInfo> {

  private static final long serialVersionUID = 1L;
  private String name;
  private TimeInfo arrival;
  private TimeInfo departure;
  private List<String> info;

  public StationInfo() {
    info = new ArrayList<String>();
  }

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

  @XmlTransient
  @Override
  public int compareTo(final StationInfo o) {
    return getSortTime().compareTo(o.getSortTime());
  }

  @XmlElement(name = "info")
  public List<String> getInfo() {
    return info;
  }

  public void setInfo(final List<String> info) {
    this.info = info;
  }

  public void addInfo(final String info) {
    this.info.add(info);
  }

  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @XmlElement(name = "arrival")
  public TimeInfo getArrival() {
    return arrival;
  }

  public void setArrival(final TimeInfo arrival) {
    this.arrival = arrival;
  }

  @XmlElement(name = "departure")
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
