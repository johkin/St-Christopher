package se.acrend.christopher.server.web.control.cron;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import se.acrend.christopher.server.dao.BookingDao;
import se.acrend.christopher.server.dao.TrainStopDao;
import se.acrend.christopher.server.util.DateUtil;

@Controller
public class PurgeDataCronController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private TrainStopDao trainStopDao;
  @Autowired
  private BookingDao bookingDao;

  @RequestMapping("/cron/delete-old-entries")
  protected void handle(final HttpServletResponse resp) throws IOException {

    try {

      Calendar yesterday = DateUtil.createCalendar();
      yesterday.add(Calendar.DAY_OF_YEAR, -1);

      trainStopDao.deleteOldEntries(yesterday);

      bookingDao.deleteOldEntries(yesterday);
    } catch (Exception e) {
      log.error("Kunde inte ta bort gammal data.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }
}
