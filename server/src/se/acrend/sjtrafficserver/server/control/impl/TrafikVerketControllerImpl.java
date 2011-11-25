package se.acrend.sjtrafficserver.server.control.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.sjtrafficserver.server.parser.TrafikVerketParser;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class TrafikVerketControllerImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());

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

    TrafikVerketParser converter = new TrafikVerketParser();

    TrainInfo info = converter.parse(new String(response.getContent(), "UTF-8"), dateString, trainNo);

    updateGuessedTime(info.getStations());

    return info;
  }

  void updateGuessedTime(final List<StationInfo> stations) {
    long delayedMillis = 0;

    Collections.sort(stations);

    for (StationInfo current : stations) {
      delayedMillis = updateGuessedTime(delayedMillis, current.getArrival());
      delayedMillis = updateGuessedTime(delayedMillis, current.getDeparture());
    }
  }

  private long updateGuessedTime(long delayedMillis, final TimeInfo time) {
    if (time != null) {
      Calendar guessed = DateUtil.createCalendar();
      guessed.setTimeInMillis(time.getOriginal().getTimeInMillis() + delayedMillis);
      time.setGuessed(guessed);
      delayedMillis = time.getDelayedMillis();
    }
    return delayedMillis;
  }
}
