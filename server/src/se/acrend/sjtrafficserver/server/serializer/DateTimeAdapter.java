package se.acrend.sjtrafficserver.server.serializer;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Apapter för datum och tid, yyyyMMdd HH:mm
 */
public class DateTimeAdapter extends XmlAdapter<String, Date> {

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");

  @Override
  public Date unmarshal(final String v) throws Exception {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String marshal(final Date date) throws Exception {
    if (date == null) {
      return null;
    }
    return sdf.format(date);
  }

}
