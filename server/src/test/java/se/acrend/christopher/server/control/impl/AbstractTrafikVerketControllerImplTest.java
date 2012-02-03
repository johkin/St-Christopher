package se.acrend.christopher.server.control.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;

public class AbstractTrafikVerketControllerImplTest {

  private AbstractTrafikVerketControllerImpl controller;

  @Before
  public void setUp() throws Exception {
    controller = new AbstractTrafikVerketControllerImpl() {

    };
  }

  @Test
  public void testUpdateGuessed() {
    List<StationInfo> stations = new ArrayList<StationInfo>();

    StationInfo station = new StationInfo();
    station.setDeparture(new TimeInfo());
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(0);
    station.getDeparture().setOriginal(cal);
    cal = Calendar.getInstance();
    cal.setTimeInMillis(60000);
    station.getDeparture().setActual(cal);
    stations.add(station);
    station = new StationInfo();
    station.setArrival(new TimeInfo());
    cal = Calendar.getInstance();
    cal.setTimeInMillis(120000);
    station.getArrival().setOriginal(cal);
    stations.add(station);

    controller.updateGuessedTime(stations, null, 0);

    assertEquals(180000, stations.get(1).getArrival().getGuessed().getTimeInMillis());

  }

}
