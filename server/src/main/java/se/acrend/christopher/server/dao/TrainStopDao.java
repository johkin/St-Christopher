package se.acrend.christopher.server.dao;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;

@Component
public class TrainStopDao {

  @Autowired
  private DatastoreService datastore;

  public List<Entity> findByTrainNo(final String trainNo, final Calendar date) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN_STOP)
        .addFilter("trainNo", FilterOperator.EQUAL, trainNo)
        .addFilter("date", FilterOperator.EQUAL, DateUtil.toDate(date)));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public List<Entity> findDepartures(final Calendar from, final Calendar to) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN_STOP)
        .addFilter("originalDeparture", FilterOperator.GREATER_THAN, DateUtil.toDate(from))
        .addFilter("originalDeparture", FilterOperator.LESS_THAN, DateUtil.toDate(to)));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public List<Entity> findArrivalsNotArrived(final Calendar from, final Calendar to) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN_STOP)
        .addFilter("originalArrival", FilterOperator.GREATER_THAN, DateUtil.toDate(from))
        .addFilter("originalArrival", FilterOperator.LESS_THAN, DateUtil.toDate(to))
        .addFilter("actualArrival", FilterOperator.EQUAL, null)
        .addFilter("originalArrival", FilterOperator.NOT_EQUAL, null));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public int deleteOldEntries(final Calendar time) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN_STOP)
        .addFilter("date", FilterOperator.LESS_THAN, DateUtil.toDate(time)));
    int counter = 0;
    for (Entity e : query.asIterable()) {
      Transaction transaction = datastore.beginTransaction();
      datastore.delete(transaction, e.getKey());
      transaction.commit();
      counter++;
    }
    return counter;
  }

  public List<Entity> findByStationName(final String stationName) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN_STOP)
        .addFilter("stationName", FilterOperator.EQUAL, stationName));

    return query.asList(FetchOptions.Builder.withDefaults());
  }
}
