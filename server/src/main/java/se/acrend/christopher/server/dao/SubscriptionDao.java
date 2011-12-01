package se.acrend.christopher.server.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import se.acrend.christopher.server.entity.SubscriptionEntity;

@Repository
public class SubscriptionDao extends AbstractDao<SubscriptionEntity> {

  public List<SubscriptionEntity> findSubscriptions() {
    return operations.find("select b from " + SubscriptionEntity.class.getName() + " b");
  }

  public SubscriptionEntity findByUser(final String email) {
    List resultList = operations.find("select b from " + SubscriptionEntity.class.getName()
        + " b where userEmail = :userEmail", email);

    if (resultList.isEmpty()) {
      return null;
    }
    return (SubscriptionEntity) resultList.get(0);
  }

}
