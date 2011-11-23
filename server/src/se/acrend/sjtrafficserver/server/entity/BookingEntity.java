package se.acrend.sjtrafficserver.server.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class BookingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  private String userEmail;
  private String registrationId;

  private Key departure;
  private Key arrival;

  private String trainNo;
  private String code;

  private Date date;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(final String registrationId) {
    this.registrationId = registrationId;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(final Key key) {
    this.key = key;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(final String userEmail) {
    this.userEmail = userEmail;
  }

  public Key getDeparture() {
    return departure;
  }

  public void setDeparture(final Key departure) {
    this.departure = departure;
  }

  public Key getArrival() {
    return arrival;
  }

  public void setArrival(final Key arrival) {
    this.arrival = arrival;
  }

  public String getTrainNo() {
    return trainNo;
  }

  public void setTrainNo(final String trainNo) {
    this.trainNo = trainNo;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(final Date date) {
    this.date = date;
  }

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

}
