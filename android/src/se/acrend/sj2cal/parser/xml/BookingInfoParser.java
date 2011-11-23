package se.acrend.sj2cal.parser.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.BookingInformation;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

public class BookingInfoParser extends AbstactResponseParser {

  private static final String TAG = "BookingInfoParser";

  public BookingInformation parser(final InputStream inputStream) {
    final BookingInformation model = new BookingInformation();

    RootElement root = new RootElement("bookingInformation");
    handleResponse(root, model);
    root.getChild("departureTrack").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setDepartureTrack(body);
      }
    });
    root.getChild("arrivalTrack").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setArrivalTrack(body);
      }
    });
    root.getChild("actualDeparture").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {

        model.setActualDeparture(parseTime(body));
      }
    });
    root.getChild("estimatedDeparture").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {

        model.setEstimatedDeparture(parseTime(body));
      }
    });
    root.getChild("guessedDeparture").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {

        model.setGuessedDeparture(parseTime(body));
      }
    });
    root.getChild("actualArrival").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {

        model.setActualArrival(parseTime(body));
      }
    });
    root.getChild("estimatedArrival").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setEstimatedArrival(parseTime(body));
      }
    });
    root.getChild("guessedArrival").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setGuessedArrival(parseTime(body));
      }
    });

    try {
      Xml.parse(inputStream, Xml.Encoding.UTF_8, root.getContentHandler());
    } catch (SAXException e) {
      Log.e(TAG, "Could not parse content.", e);
      throw new PermanentException("Could not parse content.", e);
    } catch (IOException e) {
      Log.e(TAG, "Could not read content.", e);
      throw new TemporaryException("Could not read content.", e);
    }

    return model;
  }
}
