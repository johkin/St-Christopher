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

  private Calendar original;
  private Calendar actual;
  private Calendar estimated;
  private Calendar guessed;
  private Status status;

  public TimeInfo copy() {
    TimeInfo copy = new TimeInfo();

    copy.original = original;
    copy.actual = actual;
    copy.estimated = estimated;
    copy.guessed = guessed;
    copy.status = status;

    return copy;
  }

  public void clear() {
    original = null;
    actual = null;
    estimated = null;
    guessed = null;
    status = null;
  }

  @XmlElement(name = "original")
  public Calendar getOriginal() {
    return original;
  }

  public void setOriginal(final Calendar time) {
    original = time;
  }

  @XmlElement(name = "actual")
  public Calendar getActual() {
    return actual;
  }

  public void setActual(final Calendar actual) {
    this.actual = actual;
  }

  @XmlElement(name = "estimated")
  public Calendar getEstimated() {
    return estimated;
  }

  public void setEstimated(final Calendar estimated) {
    this.estimated = estimated;
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
    if (original == null) {
      return millis;
    }
    if (actual != null) {
      millis = actual.getTimeInMillis() - original.getTimeInMillis();
    } else if (estimated != null) {
      millis = estimated.getTimeInMillis() - original.getTimeInMillis();
    } else if (guessed != null) {
      millis = guessed.getTimeInMillis() - original.getTimeInMillis();
    }
    if (millis < 0) {
      return 0;
    }
    return millis;
  }

}