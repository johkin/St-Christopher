package se.acrend.christopher.server.parser;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo.Status;
import se.acrend.christopher.shared.model.TrainInfo;

public class TrafikVerketJsonParserTest {

  private TrafikVerketJsonParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new TrafikVerketJsonParser();
  }

  @Test
  public void testParseDelayed() throws Exception {
    String content = IOUtils.toString(this.getClass().getResourceAsStream("/trafikverket/525-delayed.json"), "UTF-8");
    TrainInfo info = parser.parse(content, "20111129", "525");

    for (StationInfo station : info.getStations()) {
      if ("Lund C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
      }
      if ("Norrköping C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getArrival().getStatus());
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
      }
      if ("Stockholm C".equals(station.getName())) {
        assertEquals(Status.Delayed, station.getDeparture().getStatus());
        assertEquals(null, station.getArrival());
      }
    }

  }

}
