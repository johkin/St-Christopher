package se.acrend.christopher.server.control.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;

public class AbstractTrafikVerketControllerImpl {

  private static final int MAX_GUESS_COUNT = 3;

  protected void updateGuessedTime(final List<StationInfo> stations) {
    long delayedMillis = 0;

    Collections.sort(stations);

    int guessCount = 0;

    for (StationInfo current : stations) {
      if (guessCount > MAX_GUESS_COUNT) {
        break;
      }
      delayedMillis = updateGuessedTime(delayedMillis, current.getArrival());
      delayedMillis = updateGuessedTime(delayedMillis, current.getDeparture());
      guessCount++;
      if (hasActualTime(current.getArrival()) || hasActualTime(current.getDeparture())) {
        guessCount = 0;
      }
    }
  }

  void updateGuessedTime(final List<StationInfo> stations, final StationInfo previousStation, final int guessCount) {
    if (stations.isEmpty()) {
      return;
    }
    StationInfo currentStation = stations.get(0);
    if (previousStation != null) {
      if (hasActualTime(previousStation.getDeparture())) {
        long delayedMillis = previousStation.getDeparture().getDelayedMillis();
        updateGuessedTime(delayedMillis, currentStation.getArrival());
        updateGuessedTime(delayedMillis, currentStation.getDeparture());
      }
    }
    updateGuessedTime(stations.subList(1, stations.size()), currentStation, guessCount + 1);
  }

  private boolean hasActualTime(final TimeInfo time) {
    if (time == null) {
      return false;
    }
    if (time.getActual() != null) {
      return true;
    }
    return false;
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