package se.acrend.christopher.server.web.control;

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
import se.acrend.christopher.shared.model.AbstractResponse;
import se.acrend.christopher.shared.parser.ParserFactory;

import com.google.gson.Gson;

@Controller
public class RegistrationController {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private final TrafficServiceImpl trafficService = null;

  @RequestMapping(value = "/registration/register")
  protected void register(@RequestParam final String code, @RequestParam final String trainNo,
      @RequestParam final String date, @RequestParam final String from, @RequestParam final String to,
      @RequestParam final String registrationId, final HttpServletResponse resp) throws IOException {

    try {
      Calendar cal = DateUtil.parseDate(date);

      AbstractResponse result = trafficService.registerBooking(code, trainNo, cal.getTime(), from, to, registrationId);

      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");

      Gson gson = ParserFactory.createParser();
      gson.toJson(result, resp.getWriter());

    } catch (Exception e) {
      log.error("Kunde inte registrera bokning.", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/registration/unregister")
  protected void unregister(@RequestParam final String code, final HttpServletResponse resp) throws IOException {
    try {
      trafficService.unRegisterBooking(code);
    } catch (Exception e) {
      log.error("Kunde inte registrera bokning.", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
