package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

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
  private String track;

  public TimeInfo copy() {
    TimeInfo copy = new TimeInfo();

    copy.original = original;
    copy.actual = actual;
    copy.estimated = estimated;
    copy.guessed = guessed;
    copy.status = status;
    copy.track = track;

    return copy;
  }

  public void clear() {
    original = null;
    actual = null;
    estimated = null;
    guessed = null;
    status = null;
    track = null;
  }

  public String getTrack() {
    return track;
  }

  public void setTrack(final String track) {
    this.track = track;
  }

  public Calendar getOriginal() {
    return original;
  }

  public void setOriginal(final Calendar time) {
    original = time;
  }

  public Calendar getActual() {
    return actual;
  }

  public void setActual(final Calendar actual) {
    this.actual = actual;
  }

  public Calendar getEstimated() {
    return estimated;
  }

  public void setEstimated(final Calendar estimated) {
    this.estimated = estimated;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(final Status status) {
    this.status = status;
  }

  public Calendar getGuessed() {
    return guessed;
  }

  public void setGuessed(final Calendar guessed) {
    this.guessed = guessed;
  }

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