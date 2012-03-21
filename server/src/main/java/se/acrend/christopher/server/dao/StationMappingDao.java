package se.acrend.christopher.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.persistence.DataConstants;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@Component
public class StationMappingDao {
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

  public Entity findBySj(final String sjName) {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_BOOKING)
        .addFilter("sj", FilterOperator.EQUAL, sjName));

    return query.asSingleEntity();
  }

}
