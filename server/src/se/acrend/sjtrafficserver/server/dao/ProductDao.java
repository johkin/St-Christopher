package se.acrend.sjtrafficserver.server.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.entity.ProductEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;

public class ProductDao extends AbstractDao<ProductEntity> {

  public ProductEntity findByProductId(final String productId) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + ProductEntity.class.getName() + " b where productId = :productId");
    query.setParameter("productId", productId);

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }

    return (ProductEntity) resultList.get(0);
  }

  public List<ProductEntity> findProducts() {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + ProductEntity.class.getName() + " b");

    return query.getResultList();
  }
}
