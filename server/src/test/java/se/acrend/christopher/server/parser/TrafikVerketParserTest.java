package se.acrend.christopher.server.parser;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo.Status;
import se.acrend.christopher.shared.model.TrainInfo;

public class TrafikVerketParserTest {

  private TrafikVerketParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new TrafikVerketParser();
  }

  @Test
  public void testParseCancelled() throws Exception {
    String content = IOUtils.toString(this.getClass().getResourceAsStream("/trafikverket/284-cancelled.html"));
    TrainInfo info = parser.parse(content, "20111005", "284");

    for (StationInfo station : info.getStations()) {
      if ("Norrköping C".equals(station.getName())) {
        assertEquals(Status.Cancelled, station.getArrival().getStatus());
        assertEquals(Status.Cancelled, station.getDeparture().getStatus());
        assertEquals(4, station.getInfo().size());
      }
      if ("Stockholm C".equals(station.getName())) {
        assertEquals(Status.Cancelled, station.getArrival().getStatus());
        assertEquals(Status.Ok, station.getDeparture().getStatus());
        assertEquals(3, station.getInfo().size());
      }
    }

  }

  @Test
  public void testParseDelayed() throws Exception {
    String content = IOUtils.toString(this.getClass().getResourceAsStream("/trafikverket/542-delayed.html"));
    TrainInfo info = parser.parse(content, "20111005", "284");

    for (StationInfo station : info.getStations()) {
      if ("Alvesta".equals(station.getName())) {
        assertEquals(Status.Ok, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
        assertEquals(1, station.getInfo().size());
      }
      if ("Norrköping C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
        assertEquals(0, station.getInfo().size());
      }
      if ("Stockholm C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(null, station.getDeparture());
        assertEquals(0, station.getInfo().size());
      }
    }

  }

  @Test
  public void testParseInfo() throws Exception {
    String content = IOUtils.toString(this.getClass().getResourceAsStream("/trafikverket/523-info.html"));
    TrainInfo info = parser.parse(content, "20111005", "284");

    for (StationInfo station : info.getStations()) {
      if ("Nässjö C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
        assertEquals(3, station.getInfo().size());
      }
      if ("Hässleholm".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
        assertEquals(1, station.getInfo().size());
      }
      if ("Lund C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(null, station.getDeparture());
        assertEquals(1, station.getInfo().size());
      }
    }

  }

  @Test(expected = TemporaryException.class)
  public void testParseError() throws Exception {
    String content = IOUtils.toString(this.getClass().getResourceAsStream("/trafikverket/999-error.html"));
    parser.parse(content, "20111005", "999");
  }
}
