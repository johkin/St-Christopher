package se.acrend.christopher.server.control.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.parser.TrafikVerketParser;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.TrainInfo;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@Component("TrafikVerketController")
public class TrafikVerketControllerImpl extends AbstractTrafikVerketControllerImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private TrafikVerketParser converter;

  public TrainInfo getTagInfo(final String trainNo, final Calendar cal) throws MalformedURLException, IOException,
      UnsupportedEncodingException {

    String dateString = DateUtil.formatDate(cal);

    URL url = new URL(
        "http://www4.banverket.se/trafikinformation/(S(jd40zmnd0t4li2uzytrsdfrz))/WebPage/TrafficSituationTrain.aspx?JF=7&train="
            + dateString + "," + trainNo);
    log.debug("Hämtar info från url: {}", url.toString());

    HTTPRequest request = new HTTPRequest(url);

    URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
    HTTPResponse response = urlFetchService.fetch(request);

    log.debug("Hämtat tåg-info: {}", response);

    TrainInfo info = converter.parse(new String(response.getContent(), "UTF-8"), dateString, trainNo);

    updateGuessedTime(info.getStations());

    return info;
  }

}
