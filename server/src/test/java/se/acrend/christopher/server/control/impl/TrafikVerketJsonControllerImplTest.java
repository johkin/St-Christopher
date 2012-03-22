package se.acrend.christopher.server.control.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;

public class TrafikVerketJsonControllerImplTest {

  private TrafikVerketJsonControllerImpl controller;

  @Before
  public void setUp() throws Exception {
    controller = new TrafikVerketJsonControllerImpl();

  }

  @Test
  public void testMergeStations() {
    List<StationInfo> stations = new ArrayList<StationInfo>();
    StationInfo station = new StationInfo();
    station.setName("Umeå C");
    station.setDeparture(new TimeInfo());
    station.getDeparture().setOriginal(createCalendar(2));
    stations.add(station);

    station = new StationInfo();
    station.setName("Umeå C");
    station.setArrival(new TimeInfo());
    station.getArrival().setOriginal(createCalendar(1));
    stations.add(station);

    assertEquals(2, stations.size());

    controller.mergeStations(stations);

    assertEquals(1, stations.size());

    assertEquals(1, stations.get(0).getArrival().getOriginal().getTimeInMillis());
    assertEquals(2, stations.get(0).getDeparture().getOriginal().getTimeInMillis());
  }

  Calendar createCalendar(final long millis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(millis);
    return cal;
  }

}
