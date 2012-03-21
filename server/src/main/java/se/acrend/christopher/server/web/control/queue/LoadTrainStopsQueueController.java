package se.acrend.christopher.server.web.control.queue;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.acrend.christopher.server.service.impl.TrafficServiceImpl;
import se.acrend.christopher.server.util.DateUtil;

@Controller
public class LoadTrainStopsQueueController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private TrafficServiceImpl trafficService;

  @RequestMapping("/queue/load-train")
  protected void doPost(@RequestParam final String date, @RequestParam final String trainNo,
      final HttpServletResponse resp) throws IOException {

    try {
      Calendar cal = DateUtil.parseDate(date);

      trafficService.loadTrain(trainNo, cal);
    } catch (Exception e) {
      log.error("Kunde inte hämta information för tåg {}", trainNo, e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }
}
