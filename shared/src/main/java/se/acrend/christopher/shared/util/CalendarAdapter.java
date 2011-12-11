package se.acrend.christopher.shared.util;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CalendarAdapter implements JsonDeserializer<Calendar>, JsonSerializer<Calendar> {

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");

  static {
    sdf.setTimeZone(SharedDateUtil.SWEDISH_TIMEZONE);
  }

  @Override
  public JsonElement serialize(final Calendar calendar, final Type type, final JsonSerializationContext context) {
    return new JsonPrimitive(sdf.format(calendar.getTime()));
  }

  @Override
  public Calendar deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context)
      throws JsonParseException {

    String string = element.getAsString();
    Date date;
    try {
      date = sdf.parse(string);
    } catch (ParseException e) {
      throw new JsonParseException("Kunde inte tolka tid: " + string);
    }

    Calendar calendar = Calendar.getInstance(SharedDateUtil.SWEDISH_TIMEZONE, SharedDateUtil.SWEDISH_LOCALE);
    calendar.setTime(date);

    return calendar;
  }
}
