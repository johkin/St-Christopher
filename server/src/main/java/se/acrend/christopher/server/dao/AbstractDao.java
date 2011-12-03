package se.acrend.christopher.server.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaOperations;

import com.google.appengine.api.datastore.Key;

public class AbstractDao<T> {

  @Autowired
  JpaOperations operations;

  public List<T> findAll(final Class<?> clazz) {
    return operations.find("select e from " + clazz.getName() + " e");
  }

  public T findByKey(final Class<T> clazz, final Key key) {
    return operations.find(clazz, key);
  }

  public void create(final T entity) {
    operations.persist(entity);
  }

  public void update(final T entity) {
    operations.merge(entity);
  }

  public void delete(final T entity) {
    operations.remove(entity);
  }
}