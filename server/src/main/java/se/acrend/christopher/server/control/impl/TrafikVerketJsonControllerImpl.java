package se.acrend.christopher.server.control.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.control.TrafikverketController;
import se.acrend.christopher.server.parser.TrafikVerketJsonParser;
import se.acrend.christopher.server.parser.TrafikVerketJsonParser.TrainGroupInfo;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@Component("TrafikVerketJsonController")
public class TrafikVerketJsonControllerImpl extends AbstractTrafikVerketControllerImpl implements
    TrafikverketController {

  private static final String ORION_URL = "http://trafikinfo.trafikverket.se/litcore/orion/orionproxy.ashx";

  private static final int CACHE_SECONDS = 2 * 60 * 60;

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private TrafikVerketJsonParser parser;

  private final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

  private final MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

  /*
   * (non-Javadoc)
   * 
   * @see
   * se.acrend.christopher.server.control.impl.TrafikverketController#getTagInfo
   * (java.lang.String, java.util.Calendar)
   */
  @Override
  public TrainInfo getTagInfo(final String trainNo, final Calendar cal) {

    String date = DateUtil.formatDate(cal);

    String group = getTrainGroup(trainNo, date);

    if (group == null) {
      // TODO Felhantering
      log.debug("Hittade ingen grupp för tåg {}, datum {}", trainNo, date);
      throw new PermanentException("Kunde inte hitta tåg " + trainNo + " för datum " + date);
    }
    try {

      // TODO Hantera en lista av tåggrupp-id?

      // Kombinera svaren

      HTTPRequest request = getTrainInfo(group);

      HTTPResponse response = urlFetchService.fetch(request);

      log.debug("Hämtat tåg-info, status: {}", response.getResponseCode());

      TrainInfo info = parser.parse(new String(response.getContent(), "UTF-8"), date, trainNo);

      updateGuessedTime(info.getStations());

      return info;
    } catch (Exception e) {
      log.error("Kunde inte hämta tåg-info för tåg {}", trainNo, e);
      throw new TemporaryException("Kunde inte hämta tåg-info för tåg " + trainNo, e);
    }
  }

  String getTrainGroup(final String trainNo, final String date) {

    // TODO Hantera en lista av tåggrupp-id?

    String key = date + "-" + trainNo;

    String groupNo = (String) memcacheService.get(key);
    if (groupNo != null) {
      log.debug("Tåg {} för datum {} är cachat, grupp: {}", new String[] { trainNo, date, groupNo });
      return groupNo;
    }

    log.debug("Tåg {} är inte cachat.", trainNo);
    try {
      HTTPRequest request = createRequestForTrainNo(trainNo);

      HTTPResponse response = urlFetchService.fetch(request);

      log.debug("Hämtat tåg-info, status: {}", response.getResponseCode());

      List<TrainGroupInfo> groups = parser.parseTrainGroup(new String(response.getContent(), "UTF-8"));

      for (TrainGroupInfo info : groups) {
        if (date.equals(info.getDate())) {
          groupNo = info.getGroupNo();
        }
        key = info.getDate() + "-" + info.getTrainNo();

        if (!memcacheService.contains(key)) {
          log.debug("Cachar tåg {} för datum {}, grupp: {}",
              new String[] { info.getTrainNo(), info.getDate(), info.getGroupNo() });
          memcacheService.put(key, info.getGroupNo(), Expiration.byDeltaSeconds(CACHE_SECONDS));
          log.debug("Har cachat tåg: {}", trainNo);
        }
      }
    } catch (Exception e) {
      log.error("Kunde inte hämta tåggrupp för tåg {}", trainNo, e);
      throw new TemporaryException("Kunde inte hämta tåggrupp för tåg " + trainNo, e);
    }

    return groupNo;
  }

  private HTTPRequest createRequestForTrainNo(final String trainNo) throws MalformedURLException,
      UnsupportedEncodingException {
    String requestBody = createRequest("WOW", "LpvTrafiklagen", "AnnonseratTagId = '" + trainNo + "' ",
        "TagGrupp, AnnonseratTagId, Utgangsdatum", "Utgangsdatum ASC");

    URL url = new URL(ORION_URL);

    log.debug("Hämtar info från url: {}", url.toString());

    HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
    request.setHeader(new HTTPHeader("Content-Type", "text/xml; charset=UTF-8"));

    request.setPayload(requestBody.getBytes("UTF-8"));

    log.debug("Fråga till Trafikverket: {}", requestBody);
    return request;
  }

  private HTTPRequest getTrainInfo(final String group) throws MalformedURLException, UnsupportedEncodingException {
    String requestBody = createRequest("WOW", "LpvTrafiklagen", "TagGrupp = '" + group + "' ", null, null);

    URL url = new URL(ORION_URL);

    log.debug("Hämtar info från url: {}", url.toString());

    HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
    request.setHeader(new HTTPHeader("Content-Type", "text/xml; charset=UTF-8"));

    request.setPayload(requestBody.getBytes("UTF-8"));

    log.debug("Fråga till Trafikverket: {}", requestBody);
    return request;
  }

  String createRequest(final String plugin, final String table, final String filter, final String columns,
      final String orderBy) {
    String result = "<ORIONML version=\"1.0\"><REQUEST plugin=\"" + plugin + "\" locale=\"SE_sv\"><PLUGINML table=\""
        + table + "\"";
    if (filter != null) {
      result += " filter=\"" + filter + "\"";
    }
    if (columns != null) {
      result += " columns=\"" + columns + "\"";
    }
    if (orderBy != null) {
      result += " orderby=\"" + orderBy + "\"";
    }
    result += " /></REQUEST>" + "</ORIONML>";

    return result;
  }

}
