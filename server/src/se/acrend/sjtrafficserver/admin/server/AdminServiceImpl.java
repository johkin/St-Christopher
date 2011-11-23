package se.acrend.sjtrafficserver.admin.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.admin.client.AdminService;
import se.acrend.sjtrafficserver.admin.shared.Train;
import se.acrend.sjtrafficserver.server.dao.TrainStopDao;
import se.acrend.sjtrafficserver.server.entity.TrainStopEntity;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private TrainStopDao trainStopDao = null;

  public AdminServiceImpl() {
    trainStopDao = new TrainStopDao();
  }

  @Override
  public List<Train> getMonitoredTrains() {

    Calendar from = DateUtil.createCalendar();
    Calendar to = DateUtil.createCalendar();
    to.add(Calendar.HOUR, 2);

    List<TrainStopEntity> departures = trainStopDao.findDepartures(from, to);

    Set<Train> result = convert(departures);

    from = DateUtil.createCalendar();
    from.add(Calendar.HOUR, -4);
    to = DateUtil.createCalendar();
    to.add(Calendar.HOUR, -1);

    List<TrainStopEntity> arrivals = trainStopDao.findArrivalsNotArrived(from, to);

    result.addAll(convert(arrivals));

    List<Train> list = new ArrayList<Train>(result);

    Collections.sort(list);

    return list;
  }

  private Set<Train> convert(final List<TrainStopEntity> stops) {

    Set<Train> result = new HashSet<Train>();

    for (TrainStopEntity stop : stops) {
      Train t = new Train();
      t.setDate(DateUtil.formatDate(stop.getDate()));
      t.setTrainNo(stop.getTrainNo());
      result.add(t);
    }

    return result;
  }
}
