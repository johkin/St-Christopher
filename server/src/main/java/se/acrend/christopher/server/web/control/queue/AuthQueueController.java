package se.acrend.christopher.server.web.control.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.acrend.christopher.server.service.impl.ConfigurationServiceImpl;
import se.acrend.christopher.server.util.Constants;
import se.acrend.christopher.server.util.QueueUtil;
import se.acrend.christopher.shared.util.SharedConstants;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@Controller
public class AuthQueueController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private ConfigurationServiceImpl configurationService;

  @RequestMapping("/queue/update-server-auth")
  protected void handle(final HttpServletResponse response) throws IOException {

    try {

      String auth = callGoogleServers();
      if (auth != null) {

        Entity data = configurationService.getConfiguration();
        data.setProperty("authString", auth);
        configurationService.updateConfiguration(data);
      }
      Queue queue = QueueFactory.getQueue(QueueUtil.AUTH_QUEUE_NAME);
      queue.purge();
    } catch (Exception e) {
      log.error("Kunde hämta auth-token.", e);
      response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }

  String callGoogleServers() {

    try {

      Map<String, String> params = new HashMap<String, String>();
      params.put("Email", SharedConstants.C2DM_ACCOUNT);
      params.put("Passwd", "AndroidApp");
      params.put("accountType", "GOOGLE");
      params.put("source", "st-christopher-server");
      params.put("service", "ac2dm");

      String formData = encodeFormParams(params);
      log.info("formData: " + formData);

      URL url = new URL("https://www.google.com/accounts/ClientLogin");

      HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);

      request.setPayload(formData.getBytes());

      URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
      HTTPResponse response = urlFetchService.fetch(request);

      log.info("ResponseCode: " + response.getResponseCode());

      if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
        String content = new String(response.getContent(), Constants.ENCODING);
        log.info("Content: " + content);

        BufferedReader rd = new BufferedReader(new StringReader(content));

        String line = "";
        while ((line = rd.readLine()) != null) {
          log.debug("HttpResponse: {}", line);
          if (line.startsWith("Auth=")) {
            return line.substring(5);
          }
        }
      } else {

      }
    } catch (IOException e) {
      log.error("Kunde inte hämta authentication från Google.", e);
    }
    return null;
  }

  private String encodeFormParams(final Map<String, String> params) throws UnsupportedEncodingException {
    StringBuilder paramBuilder = new StringBuilder();
    for (String key : params.keySet()) {
      paramBuilder.append(key).append("=").append(URLEncoder.encode(params.get(key), Constants.ENCODING)).append("&");
    }

    String formData = paramBuilder.toString();
    return formData;
  }

}
