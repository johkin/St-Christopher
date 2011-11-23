package se.acrend.sjtrafficserver.admin.client;

import java.util.List;

import se.acrend.sjtrafficserver.admin.shared.Train;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdminServiceAsync {

  void getMonitoredTrains(AsyncCallback<List<Train>> callback);

}
