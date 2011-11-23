package se.acrend.sj2cal.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;

import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.sj2cal.preference.PrefsHelper;
import se.acrend.sj2cal.util.HttpUtil;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.inject.Inject;

public class ServerCommunicationHelper {

  private static final String TAG = "ServerCommunicationController";

  private static final int MAX_RETRY_COUNT = 5;

  private final HttpClient httpClient;
  private String cookie;
  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;

  public ServerCommunicationHelper() {
    httpClient = new DefaultHttpClient();
  }

  public HttpPost createPostRequest(final String contextPath) {
    return new HttpPost(HttpUtil.SERVER_URL + contextPath);
  }

  public HttpResponse callServer(final HttpPost post, final List<NameValuePair> parameters) {
    String cookie = getCookie(0);
    Log.d(TAG, "Using cookie: " + cookie);
    if (cookie != null) {
      post.setHeader("Cookie", cookie);
    }

    HttpResponse response = null;
    try {
      if ((parameters != null) && !parameters.isEmpty()) {
        post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
      }

      response = httpClient.execute(post);
      Log.d(TAG, "Response from server: " + HttpUtil.toString(response));
    } catch (ClientProtocolException e) {
      Log.e(TAG, "Protokollfel.", e);
      throw new TemporaryException("Protokollfel.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }

    return response;
  }

  private String getCookie(final int retryCount) {
    if (retryCount > MAX_RETRY_COUNT) {
      Log.e(TAG, "Har överskridit max antal försök för anrop till server.");
      throw new PermanentException("Har överskridit max antal försök för anrop till server.");
    }

    AccountManager accountManager = AccountManager.get(context);

    Log.d(TAG, "getCookie");
    HttpResponse response = null;
    try {

      if (cookie != null) {
        Log.d(TAG, "Using stored cookie");
        return cookie;
      }

      String authToken = getAuthToken();

      Log.d(TAG, "Using token: " + authToken);

      // Don't follow redirects
      httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

      HttpPost post = new HttpPost(HttpUtil.SERVER_URL + HttpUtil.LOGIN_PATH
          + URLEncoder.encode(HttpUtil.SERVER_URL + HttpUtil.REGISTRATION_PATH, "UTF-8") + "&auth=" + authToken);

      response = httpClient.execute(post);

      Log.d(TAG, "Login response " + HttpUtil.toString(response));

      if (response.getStatusLine().getStatusCode() != 302) {
        accountManager.invalidateAuthToken("com.google", authToken);

        return getCookie(retryCount + 1);
      }

      if (response.containsHeader("Set-Cookie")) {
        Header header = response.getFirstHeader("Set-Cookie");
        cookie = header.getValue();
      }
      return cookie;
    } catch (ClientProtocolException e) {
      Log.e(TAG, "Protokollfel.", e);
      throw new TemporaryException("Protokollfel.", e);
    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    } finally {
      try {
        if (response != null) {
          response.getEntity().consumeContent();
        }
      } catch (IOException ignore) {
        Log.d(TAG, "IOException", ignore);
      }
      httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
    }
  }

  private String getAuthToken() {
    Log.d(TAG, "getAuthToken");

    AccountManager accountManager = AccountManager.get(context);

    String name = prefsHelper.getAccountName();
    Account account = null;
    for (Account a : accountManager.getAccountsByType("com.google")) {
      if (name.equals(a.name)) {
        account = a;
      }
    }
    try {
      AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, "ah", false, null, null);
      Bundle result = future.getResult();

      return result.getString(AccountManager.KEY_AUTHTOKEN);
    } catch (OperationCanceledException e) {
      Log.e(TAG, "Användaren avbröt inloggning.", e);
    } catch (AuthenticatorException e) {
      Log.e(TAG, "Fel vid token-generering.", e);
    } catch (IOException e) {
      Log.e(TAG, "IO-fel vid token-generering.", e);
    }
    return null;
  }

}