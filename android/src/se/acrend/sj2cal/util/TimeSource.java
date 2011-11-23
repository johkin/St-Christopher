package se.acrend.sj2cal.util;

import java.util.Calendar;

public class TimeSource {

  public long getCurrentMillis() {
    return System.currentTimeMillis();
  }

  public Calendar getCurrentCalender() {
    return Calendar.getInstance();
  }

}
