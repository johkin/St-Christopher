package se.acrend.christopher.server.web.control;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.acrend.christopher.server.control.impl.TrafikVerketControllerImpl;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.christopher.shared.parser.ParserFactory;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.gson.Gson;

@Controller
public class TrainProxyController {

  @Autowired
  private final MemcacheService memcacheService = null;
  private final Expiration expiration = Expiration.byDeltaSeconds(60);
  @Autowired
  private TrafikVerketControllerImpl trafikVerketController;

  @RequestMapping(value = "/proxy")
  protected void doGet(@RequestParam final String trainNo, @RequestParam final String date,
      final HttpServletResponse resp) throws ServletException, IOException {

    String cacheKey = trainNo + date;

    String xml = null;
    if (!memcacheService.contains(cacheKey)) {

      Calendar cal = DateUtil.parseDate(date);

      TrainInfo info = trafikVerketController.getTagInfo(trainNo, cal);

      StringWriter writer = new StringWriter();

      Gson gson = ParserFactory.createParser();
      gson.toJson(info, writer);

      xml = writer.getBuffer().toString();

      memcacheService.put(cacheKey, xml, expiration);
    } else {
      xml = (String) memcacheService.get(cacheKey);
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    PrintWriter writer = resp.getWriter();

    writer.append(xml);

    writer.flush();
  }
}
