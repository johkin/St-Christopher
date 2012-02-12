package se.acrend.christopher.server.web.control.queue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.acrend.christopher.server.dao.BookingDao;
import se.acrend.christopher.server.dao.ServerDataDao;
import se.acrend.christopher.server.util.Constants;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@Controller
public class SendMessageQueueServlet {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private ServerDataDao dataDao;
  @Autowired
  private BookingDao bookingDao;
  @Autowired
  private DatastoreService datastore;

  @RequestMapping("/queue/send-message")
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    try {
      Entity data = dataDao.findData();

      if (!data.hasProperty("authString")) {
        sendAuthMessage();

        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        return;
      }

      Map<String, String[]> parameters = new HashMap<String, String[]>(req.getParameterMap());

      String registrationId = req.getParameter("registrationId");
      String trainNo = req.getParameter("trainNo");
      Key bookingKey = KeyFactory.stringToKey(req.getParameter("bookingKey"));

      parameters.remove("registrationId");
      parameters.remove("trainNo");
      parameters.remove("bookingKey");

      boolean result = sendMessage(bookingKey, registrationId, trainNo, (String) data.getProperty("authString"),
          parameters);

      if (!result) {
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      }

    } catch (Exception e) {
      log.error("Kunde inte skicka meddelande.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }

  private void sendAuthMessage() {
    Queue queue = QueueFactory.getQueue(Constants.AUTH_QUEUE_NAME);
    queue.add();
  }

  public boolean sendMessage(final Key bookingKey, final String registrationId, final String trainNo,
      final String auth, final Map<String, String[]> parameters) {
    try {

      Map<String, String> params = new HashMap<String, String>();
      params.put("registration_id", registrationId);
      params.put("collapse_key", trainNo);

      for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
        params.put("data." + entry.getKey(), entry.getValue()[0]);
      }

      String formData = encodeFormParams(params);
      log.info("formData: " + formData + ", auth: " + auth);
      log.info("auth: " + auth);

      URL url = new URL("https://android.apis.google.com/c2dm/send");

      HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
      request.addHeader(new HTTPHeader("Authorization", "GoogleLogin auth=" + auth));

      request.setPayload(formData.getBytes());

      URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
      HTTPResponse response = urlFetchService.fetch(request);

      List<HTTPHeader> headers = response.getHeaders();

      log.info("ResponseCode: {}", response.getResponseCode());
      log.info("Headers: {}" + toString(headers));
      String content = new String(response.getContent(), Constants.ENCODING);
      log.info("Content: {}", content);

      for (HTTPHeader header : headers) {
        if ("update-client-auth".equals(header.getName())) {
          log.debug("Försöker uppdatera auth för server");
          Entity data = dataDao.findData();
          data.setProperty("authString", header.getValue());

          Transaction transaction = datastore.beginTransaction();
          datastore.put(data);
          transaction.commit();
          // sendAuthMessage();
        }
      }

      if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
        if (content.startsWith("Error=")) {
          String errorMessage = content.substring("Error=".length());
          log.warn("Tog emot fel från c2dm-server: {}", errorMessage);
          if ("InvalidRegistration".equals(errorMessage)) {
            log.debug("Försöker ta bort bokning med ogiltig registrationId: {}", bookingKey);
            Entity booking = bookingDao.findByKey(bookingKey);
            if (booking != null) {
              Transaction transaction = datastore.beginTransaction();
              datastore.delete(booking.getKey());
              transaction.commit();
            }
          }
        }

        return true;
      } else if (response.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
        // Exponentiell backoff, kontrollera Retry-After header
        log.info("501: {}", toString(response.getHeaders()));
      } else if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        sendAuthMessage();

        log.info("Content: {}", content);
      }
    } catch (IOException e) {
      log.error("Kunde inte skicka meddelande via c2dm. ", e);
    }
    return false;
  }

  private String toString(final List<HTTPHeader> headers) {
    StringBuilder builder = new StringBuilder();

    for (HTTPHeader header : headers) {
      builder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
    }

    return builder.toString();
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
