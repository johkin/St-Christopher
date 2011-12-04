package se.acrend.christopher.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.dao.ProductDao;
import se.acrend.christopher.server.dao.ServerDataDao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

@Component
public class ConfigurationServiceImpl {

  @Autowired
  private ServerDataDao dao;

  @Autowired
  private ProductDao productDao;

  @Autowired
  private DatastoreService datastore;

  public Entity getConfiguration() {
    return dao.findData();
  }

  public void updateConfiguration(final Entity entity) {
    Transaction transaction = datastore.beginTransaction();
    datastore.put(entity);
    transaction.commit();
  }

  public void addProduct(final Entity product) {
    Transaction transaction = datastore.beginTransaction();
    datastore.put(product);
    transaction.commit();
  }

  public void updateProduct(final Entity product) {
    Transaction transaction = datastore.beginTransaction();
    datastore.put(product);
    transaction.commit();
  }

  public void deleteProduct(final Key key) {
    Transaction transaction = datastore.beginTransaction();
    datastore.delete(key);
    transaction.commit();
  }

  public List<Entity> getProducts() {
    return productDao.findProducts();
  }

}
