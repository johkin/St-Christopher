package se.acrend.christopher.android.util;

import java.util.Calendar;

public class TimeSource {

  public long getCurrentMillis() {
    return System.currentTimeMillis();
  }

  public Calendar getCurrentCalender() {
    return Calendar.getInstance();
  }

}
