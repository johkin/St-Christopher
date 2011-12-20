package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrainInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<StationInfo> stations = new ArrayList<StationInfo>();

  private String trainNo;
  private String date;
  private String lastKnownPosition;
  private Calendar lastKnownTime;
  private String lastKnownActivity;

  public String getTrainNo() {
    return trainNo;
  }

  public void setTrainNo(final String trainNo) {
    this.trainNo = trainNo;
  }

  public String getDate() {
    return date;
  }

  public void setDate(final String date) {
    this.date = date;
  }

  public List<StationInfo> getStations() {
    return stations;
  }

  public String getLastKnownPosition() {
    return lastKnownPosition;
  }

  public void setLastKnownPosition(final String lastKnownPosition) {
    this.lastKnownPosition = lastKnownPosition;
  }

  public Calendar getLastKnownTime() {
    return lastKnownTime;
  }

  public void setLastKnownTime(final Calendar lastKnownTime) {
    this.lastKnownTime = lastKnownTime;
  }

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
