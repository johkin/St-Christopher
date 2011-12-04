package se.acrend.christopher.server.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.persistence.DataConstants;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@Component
public class SubscriptionDao {

  @Autowired
  private DatastoreService datastore;

  public List<Entity> findSubscriptions() {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_SUBSCRIPTION));

    return query.asList(FetchOptions.Builder.withDefaults());
  }

  public Entity findByUser(final String email) {

    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_SUBSCRIPTION)
        .addFilter("userEmail", FilterOperator.EQUAL, email));
    Entity entity = query.asSingleEntity();

    return entity;
  }

}
