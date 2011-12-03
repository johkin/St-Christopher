package se.acrend.christopher.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.dao.ProductDao;
import se.acrend.christopher.server.dao.ServerDataDao;
import se.acrend.christopher.server.entity.ProductEntity;
import se.acrend.christopher.server.entity.ServerDataEntity;

import com.google.appengine.api.datastore.Key;

@Component
public class ConfigurationServiceImpl {

  @Autowired
  private ServerDataDao dao;

  @Autowired
  private ProductDao productDao;

  public ServerDataEntity getConfiguration() {
    return dao.findData();
  }

  public void updateConfiguration(final ServerDataEntity entity) {
    dao.update(entity);
  }

  public void addProduct(final ProductEntity product) {
    productDao.create(product);
  }

  public void updateProduct(final ProductEntity product) {
    productDao.update(product);
  }

  public void deleteProduct(final Key key) {

    ProductEntity product = productDao.findByKey(ProductEntity.class, key);
    if (product != null) {
      productDao.delete(product);
    }
  }

  public List<ProductEntity> getProducts() {
    return productDao.findProducts();
  }

}
