package se.acrend.sj2cal.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

public class DateUtil {

  private static final String TAG = "DateUtil";

  private static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");

  private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", SWEDISH_LOCALE);

  private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd HH:mm", SWEDISH_LOCALE);

  static {
    dateFormat.setTimeZone(SWEDISH_TIMEZONE);
    timeFormat.setTimeZone(SWEDISH_TIMEZONE);
  }

  public static Calendar createCalendar() {
    return Calendar.getInstance(SWEDISH_TIMEZONE);
  }

  public static String formatDate(final Date date) {
    return dateFormat.format(date);
  }

  public static String formatDate(final Calendar cal) {
    return dateFormat.format(cal.getTime());
  }

  public static Calendar parseDate(final String dateString) {
    try {
      Calendar cal = Calendar.getInstance(SWEDISH_TIMEZONE);
      Date date = dateFormat.parse(dateString);
      cal.setTime(date);
      cal.set(Calendar.HOUR, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      return cal;
    } catch (ParseException e) {
      Log.e(TAG, "Kunde inte läsa datumformat: " + dateString, e);
      throw new IllegalArgumentException("Kunde inte läsa datumformat: " + dateString, e);
    }
  }

  public static String formatTime(final Date date) {
    if (date == null) {
      return null;
    }
    return timeFormat.format(date);
  }

  public static String formatTime(final Calendar cal) {
    if (cal == null) {
      return null;
    }
    return timeFormat.format(cal.getTime());
  }

  public static Calendar parseTime(final String timeString) {
    try {
      Calendar cal = Calendar.getInstance(SWEDISH_TIMEZONE);
      Date date = timeFormat.parse(timeString);
      cal.setTime(date);
      return cal;
    } catch (ParseException e) {
      Log.e(TAG, "Kunde inte läsa datumformat: " + timeString, e);
      throw new IllegalArgumentException("Kunde inte läsa datumformat: " + timeString, e);
    }
  }

  public static Date toDate(final Calendar cal) {
    if (cal == null) {
      return null;
    }
    return new Date(cal.getTimeInMillis());
  }

  public static DateFormat createDateFormat(final String formatString) {
    SimpleDateFormat format = new SimpleDateFormat(formatString, SWEDISH_LOCALE);
    format.setTimeZone(SWEDISH_TIMEZONE);
    return format;
  }
}
