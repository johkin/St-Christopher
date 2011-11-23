package se.acrend.sjtrafficserver.server.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.entity.ServerDataEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;

public class ServerDataDao extends AbstractDao<ServerDataEntity> {

  public ServerDataEntity findData() {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select d from " + ServerDataEntity.class.getName() + " d");

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }
    return (ServerDataEntity) resultList.get(0);
  }
}
