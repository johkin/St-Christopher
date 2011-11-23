package se.acrend.sjtrafficserver.server.servlet.queue;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.service.impl.TrafficServiceImpl;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class FindChangedStopsQueueServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    try {

      String dateString = req.getParameter("date");
      Calendar cal = DateUtil.parseDate(dateString);
      String trainNo = req.getParameter("trainNo");

      TrafficServiceImpl trafficService = new TrafficServiceImpl();

      trafficService.checkTrainChanges(trainNo, cal.getTime());
    } catch (Exception e) {
      log.error("Kunde inte kontrollera aktuella tåg.", e);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    } finally {
      EMF.close();
    }
  }
}
