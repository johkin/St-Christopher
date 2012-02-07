package se.acrend.christopher.android.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import se.acrend.christopher.android.preference.PrefsHelper;
import se.acrend.christopher.android.util.HttpUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;

import com.google.inject.Inject;

public class ServerCommunicationHelper {

  private static final String TAG = "ServerCommunicationController";

  private static final int MAX_RETRY_COUNT = 5;
  private HttpContext httpContext;
  private CookieStore cookieStore;
  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;

  HttpContext getHttpContext() {
    if (httpContext == null) {
      httpContext = new BasicHttpContext();
      cookieStore = new BasicCookieStore();
      httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    return httpContext;
  }

  public HttpPost createPostRequest(final String contextPath) {
    return new HttpPost(HttpUtil.SERVER_URL + contextPath);
  }

  public <T> T callServer(final HttpPost post, final List<NameValuePair> parameters,
      final ResponseCallback<T> responseCallback) {
    getCookie(0);

    try {
      if ((parameters != null) && !parameters.isEmpty()) {
        post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
      }

      return executeRequest(post, responseCallback);

    } catch (IOException e) {
      Log.e(TAG, "Fel vid överföring till eller från server.", e);
      throw new TemporaryException("Fel vid överföring till eller från server.", e);
    }
  }

  private void getCookie(final int retryCount) {
    Log.d(TAG, "getCookie");

    if (hasCookie()) {
      return;
    }

    if (retryCount > MAX_RETRY_COUNT) {
      Log.e(TAG, "Har överskridit max antal försök för anrop till server.");
      throw new PermanentException("Har överskridit max antal försök för anrop till server.");
    }

    AccountManager accountManager = AccountManager.get(context);

    HttpResponse response = null;
    AndroidHttpClient httpClient = null;
    try {
      httpClient = AndroidHttpClient.newInstance("st-christopher-android", context);

      String authToken = getAuthToken();

      Log.d(TAG, "Using token: " + authToken);

      // Don't follow redirects
      httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

      HttpPost post = new HttpPost(HttpUtil.SERVER_URL + HttpUtil.LOGIN_PATH
          + URLEncoder.encode(HttpUtil.SERVER_URL + HttpUtil.REGISTRATION_PATH, "UTF-8") + "&auth=" + authToken);

      response = httpClient.execute(post, httpContext);

      Log.d(TAG, "Login response " + HttpUtil.toString(response));

      if ((response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) || !hasCookie()) {
        Log.d(TAG, "Invaliderar authToken");
        accountManager.invalidateAuthToken("com.google", authToken);

        getCookie(retryCount + 1);
      }

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
      if (httpClient != null) {
        httpClient.close();
      }
    }
  }

  private boolean hasCookie() {
    // Anropa för att initiera
    getHttpContext();

    for (Cookie cookie : cookieStore.getCookies()) {
      if ("SACSID".equals(cookie.getName())) {
        Log.d(TAG, "Har redan cookie");
        return true;
      }
    }
    return false;
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

  <T> T executeRequest(final HttpPost request, final ResponseCallback<T> responseCallback) {
    AndroidHttpClient httpClient = AndroidHttpClient.newInstance("st-christopher-android", context);
    HttpResponse response = null;
    try {
      response = httpClient.execute(request, httpContext);

      Log.d(TAG, "Response from server: " + HttpUtil.toString(response));

      return responseCallback.doWithResponse(response);

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

      httpClient.close();
    }

  }

  static interface ResponseCallback<T> {

    T doWithResponse(HttpResponse response) throws IOException;

  }

}