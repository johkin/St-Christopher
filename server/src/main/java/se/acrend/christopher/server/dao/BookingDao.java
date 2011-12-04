package se.acrend.christopher.server.dao;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;

@Component
public class BookingDao {
  @Autowired
  private DatastoreService datastore;

  public Entity findByKey(final Key key) {

    try {
      Entity entity = datastore.get(key);
      return entity;
    } catch (EntityNotFoundException e) {
      return null;
    }

  }

  public List<Entity> findBookings() {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public List<Entity> findBookings(final String email) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING)
        .addFilter("userEmail", FilterOperator.EQUAL, email));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  // public Entity findByTrainNoDateUser(final String trainNo, final Calendar
  // date, final String email) {
  // PreparedQuery query = datastore.prepare(new
  // Query(DataConstants.KIND_BOOKING)
  // .addFilter("trainNo", FilterOperator.EQUAL, trainNo)
  // .addFilter("date", FilterOperator.EQUAL, DateUtil.toDate(date))
  // .addFilter("userEmail", FilterOperator.EQUAL, email));
  // Entity booking = query.asSingleEntity();
  //
  // return booking;
  // }

  public Entity findByCodeUser(final String code, final String email) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING).addFilter("code",
        FilterOperator.EQUAL, code)
        .addFilter("userEmail", FilterOperator.EQUAL, email));
    Entity booking = query.asSingleEntity();

    return booking;
  }

  public List<Entity> findByDeparture(final Key key) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING)
        .addFilter("departure", FilterOperator.EQUAL, key));

    return query.asList(FetchOptions.Builder.withDefaults());

  }

  public List<Entity> findByArrival(final Key key) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING)
        .addFilter("arrival", FilterOperator.EQUAL, key));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public int deleteOldEntries(final Calendar time) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING)
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
}
