package se.acrend.christopher.android.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HttpUtil {

  public static final String SERVER_URL = "https://st-christopher-server.appspot.com";
  public static final String REGISTRATION_PATH = "/dispatch/registration";
  public static final String BILLING_PATH = "/dispatch/billing";
  public static final String LOGIN_PATH = "/_ah/login?continue=";
  public static final String PROXY_PATH = "/dispatch/proxy";

  public static String toString(final HttpResponse response) throws UnsupportedEncodingException, IOException {
    StringBuilder builder = new StringBuilder();

    builder.append("status:").append(response.getStatusLine().getStatusCode()).append("\n");

    for (Header header : response.getAllHeaders()) {
      builder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
    }
    builder.append("\n");
    // .append(IOUtils.toString(response.getEntity().getContent()));

    return builder.toString();
  }

}
