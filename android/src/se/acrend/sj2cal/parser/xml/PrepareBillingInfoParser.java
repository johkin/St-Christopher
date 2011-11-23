package se.acrend.sj2cal.parser.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.PrepareBillingInfo;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

public class PrepareBillingInfoParser extends AbstactResponseParser {

  private static final String TAG = "SubscriptionInfoParser";

  public PrepareBillingInfo parse(final InputStream inputStream) {
    final PrepareBillingInfo model = new PrepareBillingInfo();

    RootElement root = new RootElement("prepareBillingInfo");
    handleResponse(root, model);
    root.getChild("nonce").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        if (body.length() > 0) {
          model.setNonce(Long.parseLong(body));
        }
      }
    });
    root.getChild("marketLicenseKey").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        if (body.length() > 0) {
          model.setMarketLicenseKey(body);
        }
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
