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
  
  public void clear() {
    trainNo = null;
    stations.clear();
  }
}
