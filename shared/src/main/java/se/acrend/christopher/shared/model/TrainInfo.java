package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "train")
public class TrainInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<StationInfo> stations = new ArrayList<StationInfo>();

  private String trainNo;
  private String date;
  private String lastKnownPosition;
  private String lastKnownTime;
  private String lastKnownActivity;

  @XmlElement(name = "trainNo")
  public String getTrainNo() {
    return trainNo;
  }

  public void setTrainNo(final String trainNo) {
    this.trainNo = trainNo;
  }

  @XmlElement(name = "date")
  public String getDate() {
    return date;
  }

  public void setDate(final String date) {
    this.date = date;
  }

  @XmlElement(name = "station")
  public List<StationInfo> getStations() {
    return stations;
  }

  @XmlElement(name = "lastKnownPosition")
  public String getLastKnownPosition() {
    return lastKnownPosition;
  }

  public void setLastKnownPosition(final String lastKnownPosition) {
    this.lastKnownPosition = lastKnownPosition;
  }

  @XmlElement(name = "lastKnownTime")
  public String getLastKnownTime() {
    return lastKnownTime;
  }

  public void setLastKnownTime(final String lastKnownTime) {
    this.lastKnownTime = lastKnownTime;
  }

  @XmlElement(name = "lastKnownActivity")
  public String getLastKnownActivity() {
    return lastKnownActivity;
  }

  public void setLastKnownActivity(final String lastKnownActivity) {
    this.lastKnownActivity = lastKnownActivity;
  }

  public void clear() {
    trainNo = null;
    date = null;
    lastKnownActivity = null;
    lastKnownPosition = null;
    lastKnownTime = null;
    stations.clear();
  }
}
