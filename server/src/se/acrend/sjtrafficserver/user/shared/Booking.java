package se.acrend.sjtrafficserver.user.shared;

import java.io.Serializable;

public class Booking implements Serializable {

  private String userEmail;
  private String from;
  private String to;
  private String trainNo;
  private String registrationId;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(final String registrationId) {
    this.registrationId = registrationId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(final String userEmail) {
    this.userEmail = userEmail;
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

  public String getTrainNo() {
    return trainNo;
  }

  public void setTrainNo(final String trainNo) {
    this.trainNo = trainNo;
  }
}
