package se.acrend.christopher.android.parser.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

public class SubscriptionInfoParser extends AbstactResponseParser {

  private static final String TAG = "SubscriptionInfoParser";

  public SubscriptionInfo parse(final InputStream inputStream) {
    final SubscriptionInfo model = new SubscriptionInfo();

    RootElement root = new RootElement("subscriptionInfo");
    handleResponse(root, model);
    root.getChild("notificationCount").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        if (body.length() > 0) {
          model.setNotificationCount(Integer.parseInt(body));
        }
      }
    });
    root.getChild("travelWarrantCount").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        if (body.length() > 0) {
          model.setTravelWarrantCount(Integer.parseInt(body));
        }
      }
    });
    root.getChild("notificationExpireDate").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        model.setNotificationExpireDate(parseTime(body));
      }
    });
    root.getChild("travelWarrantExpireDate").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {

        model.setTravelWarrantExpireDate(parseTime(body));
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
