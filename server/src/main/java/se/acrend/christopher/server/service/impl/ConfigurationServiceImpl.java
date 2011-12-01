package se.acrend.christopher.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.dao.ServerDataDao;
import se.acrend.christopher.server.entity.ServerDataEntity;

@Component
public class ConfigurationServiceImpl {

  @Autowired
  private ServerDataDao dao;

  public ServerDataEntity getConfiguration() {
    return dao.findData();
  }

  public void updateConfiguration(final ServerDataEntity entity) {
    dao.update(entity);
  }

}
