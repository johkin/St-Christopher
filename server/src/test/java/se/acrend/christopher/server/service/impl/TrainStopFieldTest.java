package se.acrend.christopher.server.service.impl;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.server.entity.TrainStopEntity;
import se.acrend.christopher.server.service.impl.TrafficServiceImpl.TrainStopField;

public class TrainStopFieldTest {

  private TrainStopEntity oldStop;
  private TrainStopEntity newStop;

  @Before
  public void setUp() throws Exception {
    oldStop = new TrainStopEntity();
    newStop = new TrainStopEntity();
  }

  @Test
  public void testGetValue() {

    oldStop.setActualArrival(Timestamp.valueOf("2011-10-25 10:00:00"));

    String value = TrainStopField.ActualArrival.getValue(oldStop);
    assertEquals("20111025 08:00", value);
  }

  @Test
  public void testIsModifiedNot() {
    oldStop.setActualArrival(Timestamp.valueOf("2011-10-25 10:00:00"));
    newStop.setActualArrival(Timestamp.valueOf("2011-10-25 10:00:00"));

    assertEquals(false, TrainStopField.ActualArrival.isModified(oldStop, newStop));
  }

  @Test
  public void testIsModified() {
    oldStop.setActualArrival(Timestamp.valueOf("2011-10-25 10:00:00"));
    newStop.setActualArrival(Timestamp.valueOf("2011-10-25 11:00:00"));

    assertEquals(true, TrainStopField.ActualArrival.isModified(oldStop, newStop));
  }
}
