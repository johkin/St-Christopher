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
public class TrainDao {

  @Autowired
  private DatastoreService datastore;

  public Entity findByTrainNo(final String trainNo, final Calendar date) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN)
        .addFilter("trainNo", FilterOperator.EQUAL, trainNo)
        .addFilter("date", FilterOperator.EQUAL, DateUtil.toDate(date)));

    return query.asSingleEntity();
  }

  public int deleteOldEntries(final Calendar time) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN)
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

  public List<Entity> findNotLoaded() {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_TRAIN)
        .addFilter("loaded", FilterOperator.EQUAL, false));

    return query.asList(FetchOptions.Builder.withDefaults());
  }
}
