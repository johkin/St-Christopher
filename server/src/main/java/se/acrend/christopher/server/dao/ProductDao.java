package se.acrend.christopher.server.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import se.acrend.christopher.server.entity.ProductEntity;

@Repository
public class ProductDao extends AbstractDao<ProductEntity> {

  public ProductEntity findByProductId(final String productId) {
    List resultList = operations.find("select b from " + ProductEntity.class.getName()
        + " b where productId = :productId", productId);

    if (resultList.isEmpty()) {
      return null;
    }

    return (ProductEntity) resultList.get(0);
  }

  public List<ProductEntity> findProducts() {
    return operations.find("select b from " + ProductEntity.class.getName() + " b");
  }
}
