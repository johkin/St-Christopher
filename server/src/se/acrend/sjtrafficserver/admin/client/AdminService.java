package se.acrend.sjtrafficserver.admin.client;

import java.util.List;

import se.acrend.sjtrafficserver.admin.shared.Train;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("admin")
public interface AdminService extends RemoteService {

  List<Train> getMonitoredTrains();

}
