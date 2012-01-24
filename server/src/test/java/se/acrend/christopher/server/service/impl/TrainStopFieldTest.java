package se.acrend.christopher.server.service.impl;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.server.persistence.DataConstants;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TrainStopFieldTest {

  private Entity oldStop;
  private Entity newStop;

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    helper.setEnvAppId("st-cristopher-server");

    oldStop = new Entity(DataConstants.KIND_TRAIN_STOP);
    newStop = new Entity(DataConstants.KIND_TRAIN_STOP);
  }

  @Test
  public void testGetValue() {

    oldStop.setProperty("actualArrival", parseDate("2011-10-25 10:00:00"));

    String value = TrainStopField.ActualArrival.getValue(oldStop);
    assertEquals("20111025 12:00", value);
  }

  @Test
  public void testIsModifiedNot() {
    oldStop.setProperty("actualArrival", parseDate("2011-10-25 10:00:00"));
    newStop.setProperty("actualArrival", parseDate("2011-10-25 10:00:00"));

    assertEquals(false, TrainStopField.ActualArrival.isModified(oldStop, newStop));
  }

  @Test
  public void testIsModifiedLess2Min() {
    oldStop.setProperty("actualArrival", parseDate("2011-10-25 10:00:00"));
    newStop.setProperty("actualArrival", parseDate("2011-10-25 10:01:00"));

    assertEquals(false, TrainStopField.ActualArrival.isModified(oldStop, newStop));
  }

  @Test
  public void testIsModified() {
    oldStop.setProperty("actualArrival", parseDate("2011-10-25 10:00:00"));
    newStop.setProperty("actualArrival", parseDate("2011-10-25 10:02:00"));

    assertEquals(true, TrainStopField.ActualArrival.isModified(oldStop, newStop));
  }

  Date parseDate(final String value) {
    return new Date(Timestamp.valueOf(value).getTime());
  }
}
