package se.acrend.christopher.server.web.control.cron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.acrend.christopher.server.dao.TrainStopDao;
import se.acrend.christopher.server.util.Constants;
import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@Controller
public class FindCurrentTrainsCronController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private TrainStopDao trainStopDao;

  @RequestMapping(value = "/cron/check-traffic")
  public void handle(final HttpServletResponse response) throws IOException {

    try {
      List<Entity> result = new ArrayList<Entity>();

      // Hämta det som kommer att avgå inom två timmar
      Calendar from = DateUtil.createCalendar();
      Calendar to = DateUtil.createCalendar();
      to.add(Calendar.HOUR, 1);
      log.debug("Hämtar avgångar mellan {} och {}", DateUtil.toDate(from), DateUtil.toDate(to));
      List<Entity> departures = trainStopDao.findDepartures(from, to);
      result.addAll(departures);

      // Hämta det som skulle ha ankommit för minst en timme sedan
      from = DateUtil.createCalendar();
      from.add(Calendar.HOUR, -1);
      to = DateUtil.createCalendar();
      // to.add(Calendar.HOUR, -1);
      log.debug("Hämtar ankomster mellan {} och {}", DateUtil.toDate(from), DateUtil.toDate(to));
      List<Entity> arrivals = trainStopDao.findArrivalsNotArrived(from, to);
      result.addAll(arrivals);

      if (result.isEmpty()) {
        log.debug("Hittade inga bokningar.");
        return;
      }

      Set<String> currentTrains = new HashSet<String>();
      List<TaskOptions> tasks = new ArrayList<TaskOptions>();

      for (Entity stop : result) {
        String date = DateUtil.formatDate((Date) stop.getProperty("date"));
        String trainNo = (String) stop.getProperty("trainNo");
        String key = trainNo + date;
        if (!currentTrains.contains(key)) {
          log.debug("Adding train {} on {} to queue.", trainNo, date);
          tasks.add(TaskOptions.Builder.withParam("trainNo", trainNo).param("date", date));
          currentTrains.add(key);
        }
      }

      Queue queue = QueueFactory.getQueue(Constants.CURRENT_TRAINS_QUEUE_NAME);
      queue.add(tasks);

    } catch (Exception e) {
      log.error("Kunde inte kontrollera aktuella tåg.", e);
      response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }
}
