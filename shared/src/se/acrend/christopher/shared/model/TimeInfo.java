package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class TimeInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Status {
    Ok, Delayed, Cancelled
  }

  private Calendar time;
  private Calendar actual;
  private Calendar calculated;
  private Calendar guessed;
  private Status status;

  @XmlElement(name = "time")
  public Calendar getTime() {
    return time;
  }

  public void setTime(final Calendar time) {
    this.time = time;
  }

  @XmlElement(name = "actual")
  public Calendar getActual() {
    return actual;
  }

  public void setActual(final Calendar actual) {
    this.actual = actual;
  }

  @XmlElement(name = "calculated")
  public Calendar getCalculated() {
    return calculated;
  }

  public void setCalculated(final Calendar calculated) {
    this.calculated = calculated;
  }

  @XmlElement(name = "status")
  public Status getStatus() {
    return status;
  }

  public void setStatus(final Status status) {
    this.status = status;
  }

  @XmlElement(name = "guessed")
  public Calendar getGuessed() {
    return guessed;
  }

  public void setGuessed(final Calendar guessed) {
    this.guessed = guessed;
  }

  @XmlTransient
  public long getDelayedMillis() {
    long millis = 0;
    if (time == null) {
      return millis;
    }
    if (actual != null) {
      millis = actual.getTimeInMillis() - time.getTimeInMillis();
    } else if (calculated != null) {
      millis = calculated.getTimeInMillis() - time.getTimeInMillis();
    } else if (guessed != null) {
      millis = guessed.getTimeInMillis() - time.getTimeInMillis();
    }
    if (millis < 0) {
      return 0;
    }
    return millis;
  }

}