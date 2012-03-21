package se.acrend.christopher.server.web.control.cron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.acrend.christopher.server.dao.TrainDao;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.server.util.QueueUtil;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@Controller
public class LoadTrainsCronController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private TrainDao trainDao;

  @RequestMapping(value = "/cron/load-trains")
  public void handle(final HttpServletResponse response) throws IOException {

    try {
      List<Entity> trains = trainDao.findNotLoaded();

      List<TaskOptions> tasks = new ArrayList<TaskOptions>();

      for (Entity train : trains) {
        String date = DateUtil.formatDate((Date) train.getProperty("date"));
        String trainNo = (String) train.getProperty("trainNo");

        log.debug("Adding train {} on {} to load-queue.", trainNo, date);
        tasks.add(TaskOptions.Builder.withParam("trainNo", trainNo).param("date", date));
      }

      Queue queue = QueueFactory.getQueue(QueueUtil.LOAD_TRAIN_QUEUE_NAME);
      queue.add(tasks);

    } catch (Exception e) {
      log.error("Kunde inte kontrollera aktuella tåg.", e);
      response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }
}
