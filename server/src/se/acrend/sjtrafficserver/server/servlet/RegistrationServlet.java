package se.acrend.sjtrafficserver.server.servlet;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.shared.model.AbstractResponse;
import se.acrend.sjtrafficserver.server.service.impl.TrafficServiceImpl;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class RegistrationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private TrafficServiceImpl trafficService = null;

  @Override
  public void init() throws ServletException {
    super.init();
    trafficService = new TrafficServiceImpl();
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    String action = req.getParameter("action");
    String code = req.getParameter("code");
    String trainNo = req.getParameter("trainNo");
    String dateString = req.getParameter("date");
    String from = req.getParameter("from");
    String to = req.getParameter("to");
    String registrationId = req.getParameter("registrationId");
    try {
      if ("register".equals(action)) {
        Calendar date = DateUtil.parseDate(dateString);

        AbstractResponse result = trafficService.registerBooking(code, trainNo, date.getTime(), from, to,
            registrationId);

        JAXB.marshal(result, resp.getOutputStream());

      } else {
        trafficService.unRegisterBooking(code);
      }
    } catch (Exception e) {
      log.error("Kunde inte registrera bokning.", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
