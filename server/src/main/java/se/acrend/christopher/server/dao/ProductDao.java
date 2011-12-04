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
public class ProductDao {

  @Autowired
  private DatastoreService datastore;

  public Entity findByProductId(final String productId) {

    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_PRODUCT)
        .addFilter("productId", FilterOperator.EQUAL, productId));
    Entity product = query.asSingleEntity();

    return product;

  }

  public List<Entity> findProducts() {
    PreparedQuery query = datastore.prepare(new Query(DataConstants.KIND_PRODUCT));

    return query.asList(FetchOptions.Builder.withDefaults());
  }
}
