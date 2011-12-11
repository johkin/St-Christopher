package se.acrend.christopher.server.control.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;

public class AbstractTrafikVerketControllerImpl {

  protected void updateGuessedTime(final List<StationInfo> stations) {
    long delayedMillis = 0;

    Collections.sort(stations);

    for (StationInfo current : stations) {
      delayedMillis = updateGuessedTime(delayedMillis, current.getArrival());
      delayedMillis = updateGuessedTime(delayedMillis, current.getDeparture());
    }
  }

  private long updateGuessedTime(long delayedMillis, final TimeInfo time) {
    if (time != null) {
      Calendar guessed = DateUtil.createCalendar();
      guessed.setTimeInMillis(time.getOriginal().getTimeInMillis() + delayedMillis);
      time.setGuessed(guessed);
      delayedMillis = time.getDelayedMillis();
    }
    return delayedMillis;
  }

}