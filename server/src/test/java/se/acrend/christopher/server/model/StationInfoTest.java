package se.acrend.christopher.server.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;

public class StationInfoTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCompareTo() {
    List<StationInfo> list = new ArrayList<StationInfo>();

    StationInfo info = new StationInfo();
    info.setName("3");
    info.setDeparture(new TimeInfo());
    info.getDeparture().setOriginal(createCalendar(3));
    list.add(info);
    info = new StationInfo();
    info.setName("1");
    info.setDeparture(new TimeInfo());
    info.getDeparture().setOriginal(createCalendar(1));
    list.add(info);
    info = new StationInfo();
    info.setName("2");
    info.setArrival(new TimeInfo());
    info.getArrival().setOriginal(createCalendar(2));
    list.add(info);

    Collections.sort(list);

    assertEquals("1", list.get(0).getName());
    assertEquals("3", list.get(2).getName());

  }

  Calendar createCalendar(final long millis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(millis);
    return cal;
  }

}
