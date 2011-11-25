package se.acrend.sjtrafficserver.server.parser;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.StationInfo;
import se.acrend.christopher.shared.model.TimeInfo;
import se.acrend.christopher.shared.model.TimeInfo.Status;
import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class TrafikVerketParser {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String INFO_PREFIX = "Läs mer om trafikmeddelandet: ";

  private Calendar baseDate = null;

  public TrainInfo parse(final String content, final String date, final String trainNo) {

    Document document = Jsoup.parse(content);

    TrainInfo info = new TrainInfo();
    info.setDate(date);
    info.setTrainNo(trainNo);

    Elements tables = document.select("table.FavouriteDataGrid");
    if (tables.isEmpty()) {
      log.error("Kunde inte tolka svar från Transportstyrelsen för tåg {}, datum {}", trainNo, date);
      throw new TemporaryException("Kunde inte tolka svar från Transportstyrelsen för tåg " + trainNo + " datum "
          + date);
    }
    Element table = tables.first();
    Elements rows = table.select("tr");
    parseTableRows(info, rows);

    return info;
  }

  private void parseTableRows(final TrainInfo info, final Elements rows) {
    boolean first = true;
    for (Element row : rows) {
      if (first) {
        first = false;
        continue;
      }
      StationInfo station = new StationInfo();
      Elements cells = row.select("td");
      station.setName(cells.get(0).text());

      station.setArrival(parseTime(cells.get(1).text(), info.getDate()));

      station.setDeparture(parseTime(cells.get(2).text(), info.getDate()));

      station.setTrack(cells.get(3).text());
      Elements infoDivs = cells.get(4).select("div");
      for (Element infoDiv : infoDivs) {
        String infoString = null;
        Elements images = infoDiv.select("img");
        if (!images.isEmpty()) {
          infoString = images.attr("title");
          if (infoString.startsWith(INFO_PREFIX)) {
            infoString = infoString.substring(INFO_PREFIX.length());
          }
        } else {
          infoString = infoDiv.text();
        }
        if ((infoString != null) && !infoString.isEmpty()) {
          station.addInfo(infoString);
        }
      }

      info.getStations().add(station);
    }
  }

  private Status parseStatus(final String text) {
    log.debug("tolka status {}", text);
    if (text.toLowerCase().contains("inställt")) {
      return Status.Cancelled;
    }
    return Status.Ok;
  }

  private TimeInfo parseTime(final String timeString, final String date) {
    TimeInfo info = new TimeInfo();

    info.setOriginal(findValue(timeString, "^(\\d{1,2}\\:\\d{2})", date));
    if (info.getOriginal() == null) {
      return null;
    }

    info.setActual(findValue(timeString, "[Avgick|Ankom] (\\d{1,2}\\:\\d{2})", date));
    info.setEstimated(findValue(timeString, "Beräknas (\\d{1,2}\\:\\d{2})", date));

    if (info.getOriginal() != null) {
      if (info.getEstimated() != null) {
        if (info.getOriginal().before(info.getEstimated())) {
          info.setStatus(Status.Delayed);
        } else {
          info.setStatus(Status.Ok);
        }
      } else if (info.getActual() != null) {
        if (info.getOriginal().before(info.getActual())) {
          info.setStatus(Status.Delayed);
        } else {
          info.setStatus(Status.Ok);
        }
      } else {
        info.setStatus(parseStatus(timeString));
      }
    }

    return info;
  }

  protected Calendar findValue(final String text, final String patternStr, final String date) {

    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find() || (matcher.groupCount() != 1)) {
      return null;
    }

    String time = matcher.group(1);
    Calendar tempTime = DateUtil.parseTime(date + " " + time);
    if (baseDate == null) {
      baseDate = tempTime;
    }
    if (tempTime.before(baseDate)) {
      tempTime.add(Calendar.DAY_OF_YEAR, 1);
    }
    return tempTime;
  }

}
