package se.acrend.christopher.shared.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Apapter för datum och tid, yyyyMMdd HH:mm
 */
public class DateTimeAdapter extends XmlAdapter<String, Calendar> {

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");

  @Override
  public Calendar unmarshal(final String value) throws Exception {
    if (value == null) {
      return null;
    }
    Date date = sdf.parse(value);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }

  @Override
  public String marshal(final Calendar date) throws Exception {
    if (date == null) {
      return null;
    }
    return sdf.format(date.getTime());
  }

}
