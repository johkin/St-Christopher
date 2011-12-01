package se.acrend.sjtrafficserver.server.model;

import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.Test;

import se.acrend.christopher.shared.model.BookingInformation;

public class BookingInformationTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testMarshal() {

    BookingInformation information = new BookingInformation();
    information.setArrivalTrack("7");

    StringWriter writer = new StringWriter();

    JAXB.marshal(information, writer);

    System.out.println(writer.getBuffer().toString());
  }

}
