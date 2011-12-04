package se.acrend.christopher.server.entity;

import java.io.Serializable;
import java.util.Date;

import com.google.appengine.api.datastore.Key;

public class SubscriptionEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  private Key key;

  private String userEmail;
  private String registrationId;

  private int notificationCount;
  private int travelWarrantCount;

  private Date notificationExpireDate;
  private Date travelWarrantExpireDate;

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

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(final String registrationId) {
    this.registrationId = registrationId;
  }

  public int getNotificationCount() {
    return notificationCount;
  }

  public void setNotificationCount(final int notificationCount) {
    this.notificationCount = notificationCount;
  }

  public int getTravelWarrantCount() {
    return travelWarrantCount;
  }

  public void setTravelWarrantCount(final int travelWarrantCount) {
    this.travelWarrantCount = travelWarrantCount;
  }

  public Date getNotificationExpireDate() {
    return notificationExpireDate;
  }

  public void setNotificationExpireDate(final Date notificationExpireDate) {
    this.notificationExpireDate = notificationExpireDate;
  }

  public Date getTravelWarrantExpireDate() {
    return travelWarrantExpireDate;
  }

  public void setTravelWarrantExpireDate(final Date travelWarrantExpireDate) {
    this.travelWarrantExpireDate = travelWarrantExpireDate;
  }
}
