package se.acrend.sjtrafficserver.server.servlet.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.ServerDataDao;
import se.acrend.sjtrafficserver.server.entity.ServerDataEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.Constants;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class AuthQueueServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    try {

      String auth = callGoogleServers();
      if (auth != null) {
        ServerDataDao dataDao = new ServerDataDao();

        ServerDataEntity data = dataDao.findData();
        EntityTransaction transaction = EMF.getEM().getTransaction();
        transaction.begin();
        if (data == null) {
          data = new ServerDataEntity();
          data.setAuthString(auth);
          dataDao.create(data);
        } else {
          data.setAuthString(auth);
          dataDao.update(data);
        }
        transaction.commit();
        EMF.close();
      }
      Queue queue = QueueFactory.getQueue(Constants.AUTH_QUEUE_NAME);
      queue.purge();
    } catch (Exception e) {
      log.error("Kunde hämta auth-token.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }

  String callGoogleServers() {

    try {

      Map<String, String> params = new HashMap<String, String>();
      params.put("Email", "c2dm@acrend.se");
      params.put("Passwd", "AndroidApp");
      params.put("accountType", "GOOGLE");
      params.put("source", "sjtrafficserver");
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
