package se.acrend.christopher.server.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class TrainStopEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  private String trainNo;
  private Date date;

  private String stationName;
  private String trainInfo;
  private String info;
  private String track;

  private Date originalArrival;
  private Date originalDeparture;

  private Date estimatedArrival;
  private Date estimatedDeparture;

  private Date actualArrival;
  private Date actualDeparture;

  private Date guessedArrival;
  private Date guessedDeparture;

  private String arrivalStatus;
  private String departureStatus;

  public Key getKey() {
    return key;
  }

  public void setKey(final Key key) {
    this.key = key;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(final Date date) {
    this.date = date;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(final String stationName) {
    this.stationName = stationName;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(final String info) {
    this.info = info;
  }

  public String getTrack() {
    return track;
  }

  public void setTrack(final String track) {
    this.track = track;
  }

  public Date getOriginalArrival() {
    return originalArrival;
  }

  public void setOriginalArrival(final Date originalArrival) {
    this.originalArrival = originalArrival;
  }

  public Date getOriginalDeparture() {
    return originalDeparture;
  }

  public void setOriginalDeparture(final Date originalDeparture) {
    this.originalDeparture = originalDeparture;
  }

  public Date getEstimatedArrival() {
    return estimatedArrival;
  }

  public void setEstimatedArrival(final Date estimatedArrival) {
    this.estimatedArrival = estimatedArrival;
  }

  public Date getEstimatedDeparture() {
    return estimatedDeparture;
  }

  public void setEstimatedDeparture(final Date estimatedDeparture) {
    this.estimatedDeparture = estimatedDeparture;
  }

  public Date getActualArrival() {
    return actualArrival;
  }

  public void setActualArrival(final Date actualArrival) {
    this.actualArrival = actualArrival;
  }

  public Date getActualDeparture() {
    return actualDeparture;
  }

  public void setActualDeparture(final Date actualDeparture) {
    this.actualDeparture = actualDeparture;
  }

  public String getTrainInfo() {
    return trainInfo;
  }

  public void setTrainInfo(final String trainInfo) {
    this.trainInfo = trainInfo;
  }

  public String getTrainNo() {
    return trainNo;
  }

  public void setTrainNo(final String trainNo) {
    this.trainNo = trainNo;
  }

  public Date getGuessedArrival() {
    return guessedArrival;
  }

  public void setGuessedArrival(final Date guessedArrival) {
    this.guessedArrival = guessedArrival;
  }

  public Date getGuessedDeparture() {
    return guessedDeparture;
  }

  public void setGuessedDeparture(final Date guessedDeparture) {
    this.guessedDeparture = guessedDeparture;
  }

  public String getArrivalStatus() {
    return arrivalStatus;
  }

  public void setArrivalStatus(final String arrivalStatus) {
    this.arrivalStatus = arrivalStatus;
  }

  public String getDepartureStatus() {
    return departureStatus;
  }

  public void setDepartureStatus(final String departureStatus) {
    this.departureStatus = departureStatus;
  }

}
