package se.acrend.sjtrafficserver.server.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.entity.SubscriptionEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;

public class SubscriptionDao extends AbstractDao<SubscriptionEntity> {

  public List<SubscriptionEntity> findSubscriptions() {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + SubscriptionEntity.class.getName() + " b");

    return query.getResultList();
  }

  public SubscriptionEntity findByUser(final String email) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + SubscriptionEntity.class.getName()
        + " b where userEmail = :userEmail");
    query.setParameter("userEmail", email);

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }
    return (SubscriptionEntity) resultList.get(0);
  }

}
