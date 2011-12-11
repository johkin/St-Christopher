package se.acrend.christopher.shared.parser;

import java.util.Calendar;

import org.junit.Test;

import se.acrend.christopher.shared.parser.ParserFactory;

import com.google.gson.Gson;

public class ParserFactoryTest {

  @Test
  public void testCalendarSerialize() {

    Gson gson = ParserFactory.createParser();

    String json = gson.toJson(Calendar.getInstance());

    System.out.println(json);

  }

}
