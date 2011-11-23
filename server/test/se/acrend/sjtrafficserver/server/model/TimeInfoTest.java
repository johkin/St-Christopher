package se.acrend.sjtrafficserver.server.model;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.TimeInfo;

public class TimeInfoTest {

  private TimeInfo time;

  @Before
  public void setUp() throws Exception {
    time = new TimeInfo();
  }

  @Test
  public void testGetDelayedMillis() {
    time.setTime(Calendar.getInstance());
    time.getTime().setTimeInMillis(0);

    assertEquals(0, time.getDelayedMillis());

  }

  @Test
  public void testGetDelayedMillisActualOnTime() {
    time.setTime(createCalendar(0));
    time.setActual(createCalendar(0));

    assertEquals(0, time.getDelayedMillis());
  }

  @Test
  public void testGetDelayedMillisActualEarly() {
    time.setTime(createCalendar(5));
    time.setActual(createCalendar(0));

    assertEquals(0, time.getDelayedMillis());
  }

  @Test
  public void testGetDelayedMillisActualDelayed() {
    time.setTime(createCalendar(0));
    time.setActual(createCalendar(5));

    assertEquals(5, time.getDelayedMillis());
  }

  private Calendar createCalendar(final long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);
    return calendar;
  }
}
