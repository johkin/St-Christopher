package se.acrend.sjtrafficserver.server.servlet.cron;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.BookingDao;
import se.acrend.sjtrafficserver.server.dao.TrainStopDao;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class PurgeDataCronServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    try {

      Calendar yesterday = DateUtil.createCalendar();
      yesterday.add(Calendar.DAY_OF_YEAR, -1);

      TrainStopDao trainStopDao = new TrainStopDao();
      BookingDao bookingDao = new BookingDao();

      trainStopDao.deleteOldEntries(yesterday);

      bookingDao.deleteOldEntries(yesterday);

    } catch (Exception e) {
      log.error("Kunde inte ta bort gammal data.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } finally {
      EMF.close();
    }
  }
}
