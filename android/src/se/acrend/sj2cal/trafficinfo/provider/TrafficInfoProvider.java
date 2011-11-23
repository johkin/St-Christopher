package se.acrend.sj2cal.trafficinfo.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;

import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.sj2cal.model.DbModel.TimeModel;
import se.acrend.sj2cal.parser.xml.ProxyParser;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class TrafficInfoProvider extends ContentProvider {

  public static final String[] PROJECTION = new String[] { TrafficInfoProviderTypes.Columns.TRAIN_NO,
      TrafficInfoProviderTypes.Columns.NAME, TrafficInfoProviderTypes.Columns.TRACK,
      TrafficInfoProviderTypes.Columns.INFO, TrafficInfoProviderTypes.Columns.DEPARTURE_TIME,
      TrafficInfoProviderTypes.Columns.DEPARTURE_CALCULATED, TrafficInfoProviderTypes.Columns.DEPARTURE_ACTUAL,
      TrafficInfoProviderTypes.Columns.ARRIVAL_TIME, TrafficInfoProviderTypes.Columns.ARRIVAL_CALCULATED,
      TrafficInfoProviderTypes.Columns.ARRIVAL_ACTUAL };

  private static final String TAG = "TrafficInfoProvider";
  private static final String URL = "http://sjtrafficserver.appspot.com/proxy?trainNo=%s&date=%s";

  private static final int STATION = 1;
  private static final int TRAIN = 2;

  private UriMatcher uriMatcher;

  @Override
  public boolean onCreate() {

    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    uriMatcher.addURI(TrafficInfoProviderTypes.AUTHORITY, TrafficInfoProviderTypes.PATH_SINGLE, STATION);
    uriMatcher.addURI(TrafficInfoProviderTypes.AUTHORITY, TrafficInfoProviderTypes.PATH_MULTIPLE, TRAIN);
    return true;
  }

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
      final String sortOrder) {
    MatrixCursor cursor = null;

    List<String> segments = uri.getPathSegments();

    ProxyParser parser = new ProxyParser();

    String url = String.format(URL, segments.get(1), segments.get(0));
    AndroidHttpClient client = null;
    try {
      Log.d(TAG, "Reading from Url: " + url);

      client = AndroidHttpClient.newInstance("sj2cal");

      HttpGet get = new HttpGet(url);

      HttpResponse response = client.execute(get);
      InputStream content = response.getEntity().getContent();
      TrainInfo trainInfo = parser.parse(content);
      content.close();

      cursor = new MatrixCursor(PROJECTION);

      if (segments.size() == 3) {
        String stationName = segments.get(2);
        for (StationModel station : trainInfo.getStations()) {
          if (stationName.equals(station.getName())) {
            RowBuilder newRow = cursor.newRow();
            convert(trainInfo, newRow);
            convert(station, newRow);
          }
        }
      } else {
        for (StationModel station : trainInfo.getStations()) {
          RowBuilder newRow = cursor.newRow();
          convert(trainInfo, newRow);
          convert(station, newRow);
        }
      }

      Log.d(TAG, "Efter anrop");
    } catch (ConnectTimeoutException e) {
      Log.e(TAG, "Timeout från Proxy. Url: " + url, e);
      return null;
    } catch (IOException e) {
      Log.e(TAG, "Kunde inte läsa från proxy. Url: " + url, e);
      throw new RuntimeException("Kunde inte läsa från proxy. Url: " + url, e);
    } finally {
      if (client != null) {
        client.close();
      }
    }

    return cursor;
  }

  private void convert(final TrainInfoModel trainInfo, final RowBuilder newRow) {
    newRow.add(trainInfo.getTrainNo());
  }

  private void convert(final StationModel station, final RowBuilder newRow) {
    newRow.add(station.getName());
    newRow.add(station.getTrack());
    convert(station.getDeparture(), newRow);
    convert(station.getArrival(), newRow);
  }

  private void convert(final TimeModel time, final RowBuilder newRow) {
    if (time != null) {
      newRow.add(time.getTime());
      newRow.add(time.getCalculated());
      newRow.add(time.getActual());
    } else {
      newRow.add(null);
      newRow.add(null);
      newRow.add(null);
    }
  }

  @Override
  public String getType(final Uri uri) {
    switch (uriMatcher.match(uri)) {
    case STATION:
      return TrafficInfoProviderTypes.STATION_ITEM_TYPE;
    case TRAIN:
      return TrafficInfoProviderTypes.STATION_MULTIPLE_TYPE;
    default:
      throw new IllegalArgumentException("Uknonw URI: " + uri);
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    return 0;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    return null;
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    return 0;
  }

}
