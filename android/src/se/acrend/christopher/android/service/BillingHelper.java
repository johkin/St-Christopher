package se.acrend.christopher.android.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import se.acrend.christopher.android.parser.xml.PrepareBillingInfoParser;
import se.acrend.christopher.android.parser.xml.ProductParser;
import se.acrend.christopher.android.parser.xml.SubscriptionInfoParser;
import se.acrend.christopher.android.util.HttpUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import android.util.Log;

import com.google.inject.Inject;

public class BillingHelper {

  private static final String TAG = "BillingHelper";
  @Inject
  private ServerCommunicationHelper communicationHelper;
  @Inject
  private SubscriptionInfoParser subscriptionInfoParser;
  @Inject
  private PrepareBillingInfoParser prepareBillingInfoParser;
  @Inject
  private ProductParser productParser;

  public SubscriptionInfo getSubscriptionInfo() {
    Log.d(TAG, "Hämta prenumeration");

    try {

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.BILLING_PATH + "/getSubscription");

      List<NameValuePair> parameters = new ArrayList<NameValuePair>();

      HttpResponse response = communicationHelper.callServer(post, parameters);

      SubscriptionInfo information = subscriptionInfoParser.parse(response.getEntity().getContent());

      return information;

    } catch (IllegalStateException e) {
      Log.e(TAG, "Felaktigt tillstånd för att hämta innehåll från servern.", e);
      throw new PermanentException("Felaktigt tillstånd för att hämta innehåll från servern.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }
  }

  public ProductList getProductList() {
    Log.d(TAG, "Hämta produkter");

    try {

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.BILLING_PATH + "/getProductList");

      List<NameValuePair> parameters = new ArrayList<NameValuePair>();

      HttpResponse response = communicationHelper.callServer(post, parameters);

      ProductList information = productParser.parse(response.getEntity().getContent());

      return information;
    } catch (IllegalStateException e) {
      Log.e(TAG, "Felaktigt tillstånd för att hämta innehåll från servern.", e);
      throw new PermanentException("Felaktigt tillstånd för att hämta innehåll från servern.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }
  }

  public String getMarketLicenseKey() {
    Log.d(TAG, "Hämta Market-Key");

    try {
      HttpPost post = communicationHelper.createPostRequest(HttpUtil.BILLING_PATH + "/getMarketLicenseKey");

      List<NameValuePair> parameters = new ArrayList<NameValuePair>();

      HttpResponse response = communicationHelper.callServer(post, parameters);

      PrepareBillingInfo billingInfo = prepareBillingInfoParser.parse(response.getEntity().getContent());

      return billingInfo.getMarketLicenseKey();

    } catch (IllegalStateException e) {
      Log.e(TAG, "Felaktigt tillstånd för att hämta innehåll från servern.", e);
      throw new PermanentException("Felaktigt tillstånd för att hämta innehåll från servern.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }
  }

  public SubscriptionInfo sendBillingCompleted(final String productId, final long nonce) {
    Log.d(TAG, "Skicka transaktion slutförd");

    try {

      HttpPost post = communicationHelper.createPostRequest(HttpUtil.BILLING_PATH + "/billingCompleted");

      List<NameValuePair> parameters = new ArrayList<NameValuePair>();
      parameters.add(new BasicNameValuePair("productId", productId));
      parameters.add(new BasicNameValuePair("nonce", Long.toString(nonce)));

      HttpResponse response = communicationHelper.callServer(post, parameters);

      SubscriptionInfo information = subscriptionInfoParser.parse(response.getEntity().getContent());

      return information;

    } catch (IllegalStateException e) {
      Log.e(TAG, "Felaktigt tillstånd för att hämta innehåll från servern.", e);
      throw new PermanentException("Felaktigt tillstånd för att hämta innehåll från servern.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }
  }

}