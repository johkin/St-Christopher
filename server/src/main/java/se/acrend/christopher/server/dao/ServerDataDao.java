package se.acrend.christopher.server.dao;

import org.springframework.stereotype.Repository;

import se.acrend.christopher.server.entity.ServerDataEntity;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Repository
public class ServerDataDao extends AbstractDao<ServerDataEntity> {

  private final Key key = KeyFactory.createKey("ServerDataEntity", 1);

  public ServerDataEntity findData() {
    ServerDataEntity entity = operations.find(ServerDataEntity.class, key);

    if (entity == null) {
      entity = new ServerDataEntity();
      entity.setKey(key);
    }
    return entity;
  }
}
