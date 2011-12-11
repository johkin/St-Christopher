package se.acrend.christopher.android.parser.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TrainInfo;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

/**
 * Parser som hanterar ett komplett tåg.
 */
public class ProxyParser extends AbstactResponseParser {

  private static final String TAG = "ProxyParser";

  public TrainInfo parse(final InputStream is) {
    final TrainInfo model = new TrainInfo();
    RootElement root = new RootElement("train");

    root.setStartElementListener(new StartElementListener() {

      @Override
      public void start(final Attributes attributes) {
        model.clear();
      }
    });

    root.getChild("trainNo").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setTrainNo(body);
      }
    });
    final StationInfo stationModel = new StationInfo();
    final TimeInfo arrival = new TimeInfo();
    final TimeInfo departure = new TimeInfo();
    Element station = root.getChild("station");
    station.setElementListener(new ElementListener() {

      @Override
      public void end() {
        model.getStations().add(stationModel.copy());
      }

      @Override
      public void start(final Attributes attributes) {
        stationModel.clear();
      }
    });
    station.getChild("name").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        stationModel.setName(body);
      }
    });
    Element arrivalElmt = station.getChild("arrival");
    arrivalElmt.getChild("original").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        arrival.setOriginal(parseTime(body));
      }
    });
    arrivalElmt.getChild("actual").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        arrival.setActual(parseTime(body));
      }
    });
    arrivalElmt.getChild("estimated").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        arrival.setEstimated(parseTime(body));
      }
    });
    arrivalElmt.getChild("track").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        arrival.setTrack(body);
      }
    });
    arrivalElmt.setElementListener(new ElementListener() {

      @Override
      public void end() {
        stationModel.setArrival(arrival.copy());
      }

      @Override
      public void start(final Attributes attributes) {
        arrival.clear();
      }
    });
    Element departureElmt = station.getChild("departure");
    departureElmt.getChild("original").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        departure.setOriginal(parseTime(body));
      }
    });
    departureElmt.getChild("actual").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        departure.setActual(parseTime(body));
      }
    });
    departureElmt.getChild("estimated").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        departure.setEstimated(parseTime(body));
      }
    });
    departureElmt.getChild("track").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        departure.setTrack(body);
      }
    });
    departureElmt.setElementListener(new ElementListener() {

      @Override
      public void end() {
        stationModel.setDeparture(departure.copy());
      }

      @Override
      public void start(final Attributes attributes) {
        departure.clear();
      }
    });

    try {
      Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
    } catch (SAXException e) {
      Log.e(TAG, "Could not parse content.", e);
      throw new PermanentException("Could not parse content.", e);
    } catch (IOException e) {
      Log.e(TAG, "Could not read content.", e);
      throw new TemporaryException("Could not read content.", e);
    }

    return model;
  }

  @Override
  protected Calendar parseTime(final String time) {
    if (time == null) {
      return null;
    }
    if (time.length() == 0) {
      return null;
    }
    return DateUtil.parseTime(time);
  }
}
