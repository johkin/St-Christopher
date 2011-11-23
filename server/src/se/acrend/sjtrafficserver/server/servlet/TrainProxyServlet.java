package se.acrend.sjtrafficserver.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;

import se.acrend.christopher.shared.model.TrainInfo;
import se.acrend.sjtrafficserver.server.control.impl.TrafikVerketControllerImpl;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class TrainProxyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private MemcacheService memcacheService = null;
  private Expiration expiration = null;
  private TrafikVerketControllerImpl trafikVerketController;

  @Override
  public void init() throws ServletException {
    super.init();
    expiration = Expiration.byDeltaSeconds(60);
    trafikVerketController = new TrafikVerketControllerImpl();
    memcacheService = MemcacheServiceFactory.getMemcacheService();
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
      IOException {

    String trainNo = req.getParameter("trainNo");
    String dateString = req.getParameter("date");

    String cacheKey = trainNo + dateString;

    String xml = null;
    if (!memcacheService.contains(cacheKey)) {

      Calendar date = DateUtil.parseDate(dateString);

      TrainInfo info = trafikVerketController.getTagInfo(trainNo, date);

      StringWriter writer = new StringWriter();

      JAXB.marshal(info, writer);
      xml = writer.getBuffer().toString();

      memcacheService.put(cacheKey, xml, expiration);
    } else {
      xml = (String) memcacheService.get(cacheKey);
    }

    resp.setContentType("text/xml");
    resp.setCharacterEncoding("UTF-8");

    PrintWriter writer = resp.getWriter();

    writer.append(xml);

    writer.flush();
  }
}
