package se.acrend.sjtrafficserver.admin.shared;

import java.io.Serializable;

public class Train implements Serializable, Comparable<Train> {

  private static final long serialVersionUID = 1L;

  private String trainNo;
  private String date;

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

  @Override
  public int compareTo(final Train o) {
    String other = o.date + o.trainNo;
    String key = date + trainNo;

    return key.compareTo(other);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Train) {
      Train o = (Train) obj;
      return this.compareTo(o) == 0;
    }
    return false;
  }

}
