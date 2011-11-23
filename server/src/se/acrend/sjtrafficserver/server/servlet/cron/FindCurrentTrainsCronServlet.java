package se.acrend.sjtrafficserver.server.servlet.cron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.TrainStopDao;
import se.acrend.sjtrafficserver.server.entity.TrainStopEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.Constants;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class FindCurrentTrainsCronServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    try {

      TrainStopDao trainStopDao = new TrainStopDao();

      List<TrainStopEntity> result = new ArrayList<TrainStopEntity>();

      // Hämta det som kommer att avgå inom två timmar
      Calendar from = DateUtil.createCalendar();
      Calendar to = DateUtil.createCalendar();
      to.add(Calendar.HOUR, 2);
      List<TrainStopEntity> departures = trainStopDao.findDepartures(from, to);
      result.addAll(departures);

      // Hämta det som skulle ha ankommit för minst en timme sedan
      from = DateUtil.createCalendar();
      from.add(Calendar.HOUR, -4);
      to = DateUtil.createCalendar();
      to.add(Calendar.HOUR, -1);
      List<TrainStopEntity> arrivals = trainStopDao.findArrivalsNotArrived(from, to);
      result.addAll(arrivals);

      if (result.isEmpty()) {
        return;
      }

      Set<String> currentTrains = new HashSet<String>();
      List<TaskOptions> tasks = new ArrayList<TaskOptions>();

      for (TrainStopEntity stop : result) {
        String date = DateUtil.formatDate(stop.getDate());
        String key = stop.getTrainNo() + date;
        if (!currentTrains.contains(key)) {
          log.debug("Adding train {} on {} to queue.", stop.getTrainNo(), date);
          tasks.add(TaskOptions.Builder.withParam("trainNo", stop.getTrainNo()).param("date", date));
          currentTrains.add(key);
        }
      }

      Queue queue = QueueFactory.getQueue(Constants.CURRENT_TRAINS_QUEUE_NAME);
      queue.add(tasks);

    } catch (Exception e) {
      log.error("Kunde inte kontrollera aktuella tåg.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } finally {
      EMF.close();
    }
  }
}
