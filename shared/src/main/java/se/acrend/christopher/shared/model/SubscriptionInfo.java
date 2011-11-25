package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import se.acrend.christopher.shared.util.DateTimeAdapter;

@XmlRootElement(name = "subscriptionInfo")
public class SubscriptionInfo extends AbstractResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private int notificationCount;
  private int travelWarrantCount;

  private Calendar notificationExpireDate;
  private Calendar travelWarrantExpireDate;

  @XmlElement(name = "notificationCount")
  public int getNotificationCount() {
    return notificationCount;
  }

  public void setNotificationCount(final int notificationCount) {
    this.notificationCount = notificationCount;
  }

  @XmlElement(name = "travelWarrantCount")
  public int getTravelWarrantCount() {
    return travelWarrantCount;
  }

  public void setTravelWarrantCount(final int travelWarrantCount) {
    this.travelWarrantCount = travelWarrantCount;
  }

  @XmlElement(name = "notificationExpireDate")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getNotificationExpireDate() {
    return notificationExpireDate;
  }

  public void setNotificationExpireDate(final Calendar notificationExpireDate) {
    this.notificationExpireDate = notificationExpireDate;
  }

  @XmlElement(name = "travelWarrantExpireDate")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getTravelWarrantExpireDate() {
    return travelWarrantExpireDate;
  }

  public void setTravelWarrantExpireDate(final Calendar travelWarrantExpireDate) {
    this.travelWarrantExpireDate = travelWarrantExpireDate;
  }

}
