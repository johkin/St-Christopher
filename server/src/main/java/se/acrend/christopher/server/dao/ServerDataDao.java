package se.acrend.christopher.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.persistence.DataConstants;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Component
public class ServerDataDao {

  private final Key key = KeyFactory.createKey(DataConstants.KIND_SERVER_DATA, 1);

  @Autowired
  private DatastoreService datastore;

  public Entity findData() {
    Entity entity = null;
    try {
      entity = datastore.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(DataConstants.KIND_SERVER_DATA, key);
    }
    return entity;
  }
}
