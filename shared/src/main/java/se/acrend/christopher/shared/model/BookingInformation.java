package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

public class BookingInformation extends AbstractResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String departureTrack;
  private String departureInfo;

  private String arrivalTrack;
  private String arrivalInfo;

  private Calendar originalDeparture;
  private Calendar actualDeparture;
  private Calendar estimatedDeparture;
  private Calendar guessedDeparture;

  private Calendar originalArrival;
  private Calendar actualArrival;
  private Calendar estimatedArrival;
  private Calendar guessedArrival;

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

  public Calendar getActualDeparture() {
    return actualDeparture;
  }

  public void setActualDeparture(final Calendar actualDeparture) {
    this.actualDeparture = actualDeparture;
  }

  public Calendar getEstimatedDeparture() {
    return estimatedDeparture;
  }

  public void setEstimatedDeparture(final Calendar estimatedDeparture) {
    this.estimatedDeparture = estimatedDeparture;
  }

  public Calendar getEstimatedArrival() {
    return estimatedArrival;
  }

  public void setEstimatedArrival(final Calendar estimatedArrival) {
    this.estimatedArrival = estimatedArrival;
  }

  public Calendar getActualArrival() {
    return actualArrival;
  }

  public void setActualArrival(final Calendar actualArrival) {
    this.actualArrival = actualArrival;
  }

  public Calendar getGuessedArrival() {
    return guessedArrival;
  }

  public void setGuessedArrival(final Calendar guessedArrival) {
    this.guessedArrival = guessedArrival;
  }

  public Calendar getGuessedDeparture() {
    return guessedDeparture;
  }

  public void setGuessedDeparture(final Calendar guessedDeparture) {
    this.guessedDeparture = guessedDeparture;
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

  public Calendar getOriginalDeparture() {
    return originalDeparture;
  }

  public void setOriginalDeparture(final Calendar originalDeparture) {
    this.originalDeparture = originalDeparture;
  }

  public Calendar getOriginalArrival() {
    return originalArrival;
  }

  public void setOriginalArrival(final Calendar originalArrival) {
    this.originalArrival = originalArrival;
  }

}
