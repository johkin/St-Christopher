package se.acrend.christopher.android.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HttpUtil {

  public static final String SERVER_URL = "https://1.latest.st-christopher-server.appspot.com";
  public static final String REGISTRATION_PATH = "/registration";
  public static final String BILLING_PATH = "/billing";
  public static final String LOGIN_PATH = "/_ah/login?continue=";

  public static String toString(final HttpResponse response) throws UnsupportedEncodingException, IOException {
    StringBuilder builder = new StringBuilder();

    for (Header header : response.getAllHeaders()) {
      builder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
    }
    builder.append("\n");

    // HttpEntity entity = response.getEntity();
    // BufferedReader reader = new BufferedReader(new
    // InputStreamReader(entity.getContent(), "UTF-8"));
    //
    // String line = "";
    // while ((line = reader.readLine()) != null) {
    // builder.append(line).append("\n");
    // }
    return builder.toString();
  }

}
