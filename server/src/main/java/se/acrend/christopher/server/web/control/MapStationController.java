package se.acrend.christopher.server.web.control;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.acrend.christopher.server.service.impl.TrafficServiceImpl;

@Controller
public class MapStationController {

  @Autowired
  private TrafficServiceImpl trafficService;

  @RequestMapping(value = "/mapStationName")
  protected void doGet(@RequestParam final String from, @RequestParam final String to,
      final HttpServletResponse resp) throws ServletException, IOException {

    trafficService.createStationNameMapping(from, to);

    PrintWriter writer = resp.getWriter();
    writer.append("Tack!");
  }
}
