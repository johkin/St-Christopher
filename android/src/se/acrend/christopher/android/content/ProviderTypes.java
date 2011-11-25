package se.acrend.christopher.android.content;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProviderTypes {

  static final String TICKET_MIME_TYPE = "se.acrend.christopher.ticket";

  public static final String TICKET_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TICKET_MIME_TYPE;
  public static final String TICKET_MULTIPLE_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TICKET_MIME_TYPE;

  public static final String AUTHORITY = "se.acrend.christopher.ticket";
  public static final String PATH_SINGLE = "tickets/#";
  public static final String PATH_MULTIPLE = "tickets";

  public static final Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/"
      + PATH_MULTIPLE);

  public static class Columns implements BaseColumns {

    public static final String TICKET_CODE = "ticketCode";
    public static final String TICKET_TEXT = "ticketText";
    public static final String TRAIN_NO = "trainNo";
    public static final String SEAT = "seat";
    public static final String CAR = "car";
    public static final String ORIGINAL_DEPARTURE = "originalDeparture";
    public static final String ACTUAL_DEPARTURE = "actualDeparture";
    public static final String ESTIMATED_DEPARTURE = "estimatedDeparture";
    public static final String GUESSED_DEPARTURE = "guessedDeparture";
    public static final String ORIGINAL_ARRIVAL = "originalArrival";
    public static final String ACTUAL_ARRIVAL = "actualArrival";
    public static final String ESTIMATED_ARRIVAL = "estimatedArrival";
    public static final String GUESSED_ARRIVAL = "guessedArrival";
    public static final String FROM = "fromStation";
    public static final String TO = "toStation";
    public static final String NOTIFY = "notify";
    public static final String REGISTERED = "registered";
    public static final String CALENDAR_EVENT_URI = "calendarEventUri";
    public static final String DEPARTURE_TRACK = "departureTrack";
    public static final String ARRIVAL_TRACK = "arrivalTrack";
    public static final String DEPARTURE_INFO = "departureInfo";
    public static final String ARRIVAL_INFO = "arrivalInfo";

  }
}
