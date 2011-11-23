package se.acrend.sjtrafficserver.server.servlet.queue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.BookingDao;
import se.acrend.sjtrafficserver.server.dao.ServerDataDao;
import se.acrend.sjtrafficserver.server.entity.BookingEntity;
import se.acrend.sjtrafficserver.server.entity.ServerDataEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.Constants;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class SendMessageQueueServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    try {

      ServerDataDao dataDao = new ServerDataDao();

      ServerDataEntity data = dataDao.findData();

      if (data == null) {
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

      boolean result = sendMessage(bookingKey, registrationId, trainNo, data.getAuthString(), parameters);

      if (!result) {
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      }

    } catch (Exception e) {
      log.error("Kunde inte skicka meddelande.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } finally {
      EMF.close();
    }
  }

  private void sendAuthMessage() {
    Queue queue = QueueFactory.getQueue(Constants.AUTH_QUEUE_NAME);
    queue.add();
  }

  public boolean sendMessage(final Key bookingKey, final String registrationId, final String trainNo,
      final String auth,
      final Map<String, String[]> parameters) {
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
          sendAuthMessage();
        }
      }

      if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
        if (content.startsWith("Error=")) {
          String errorMessage = content.substring("Error=".length());
          log.warn("Tog emot fel från c2dm-server: {}", errorMessage);
          if ("InvalidRegistration".equals(errorMessage)) {
            log.debug("Försöker ta bort bokning med ogiltig registrationId: {}", bookingKey);
            try {
              BookingDao bookingDao = new BookingDao();
              BookingEntity booking = bookingDao.findByKey(bookingKey);
              if (booking != null) {
                bookingDao.delete(booking);
              }
            } finally {
              EMF.close();
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
