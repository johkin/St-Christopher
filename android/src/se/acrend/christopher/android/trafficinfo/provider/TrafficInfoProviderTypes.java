package se.acrend.christopher.android.trafficinfo.provider;

import android.content.ContentResolver;
import android.net.Uri;

public class TrafficInfoProviderTypes {

  static final String STATION_MIME_SUFFIX = "se.acrend.christopher.station";

  public static final String STATION_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + STATION_MIME_SUFFIX;
  public static final String STATION_MULTIPLE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + STATION_MIME_SUFFIX;

  public static final String AUTHORITY = "se.acrend.christopher.trafficInfo";
  public static final String PATH_SINGLE = "train/#/#/#";
  public static final String PATH_MULTIPLE = "train/#/#";

  public static final Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/train");

  public static class Columns {
    public static String TRAIN_NO = "trainNo";
    public static String NAME = "name";
    public static String TRACK = "track";
    public static String INFO = "info";
    public static String DEPARTURE_TIME = "departureTime";
    public static String DEPARTURE_CALCULATED = "calculatedDepartureTime";
    public static String DEPARTURE_ACTUAL = "actualDepartureTime";
    public static String ARRIVAL_TIME = "arrivalTime";
    public static String ARRIVAL_CALCULATED = "calculatedArrivalTime";
    public static String ARRIVAL_ACTUAL = "actualArrivalTime";
  }
}
