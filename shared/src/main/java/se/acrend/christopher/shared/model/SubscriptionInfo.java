package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

public class SubscriptionInfo extends AbstractResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private int notificationCount;
  private int travelWarrantCount;

  private Calendar notificationExpireDate;
  private Calendar travelWarrantExpireDate;

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

  public Calendar getNotificationExpireDate() {
    return notificationExpireDate;
  }

  public void setNotificationExpireDate(final Calendar notificationExpireDate) {
    this.notificationExpireDate = notificationExpireDate;
  }

  public Calendar getTravelWarrantExpireDate() {
    return travelWarrantExpireDate;
  }

  public void setTravelWarrantExpireDate(final Calendar travelWarrantExpireDate) {
    this.travelWarrantExpireDate = travelWarrantExpireDate;
  }

}
