package se.acrend.christopher.shared.parser;

import java.util.Calendar;
import java.util.GregorianCalendar;

import se.acrend.christopher.shared.util.CalendarAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ParserFactory {

  private static Gson instance = null;

  public static Gson createParser() {
    if (instance == null) {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(Calendar.class, new CalendarAdapter());
      builder.registerTypeAdapter(GregorianCalendar.class, new CalendarAdapter());
      instance = builder.create();
    }
    return instance;
  }

}
