package se.acrend.sjtrafficserver.server.service.impl;

import org.junit.Before;
import org.junit.Test;

import se.acrend.sjtrafficserver.server.service.impl.TrafficServiceImpl.TrainStopField;

public class TrafficServiceImplTest {

  private TrafficServiceImpl service;

  @Before
  public void setUp() throws Exception {
    service = new TrafficServiceImpl();
  }

  @Test
  public void test() {
    TrainStopField f = TrainStopField.ActualArrival;
  }

}
