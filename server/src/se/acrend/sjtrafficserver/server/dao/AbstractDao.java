package se.acrend.sjtrafficserver.server.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.persistence.EMF;

public class AbstractDao<T> {

  public AbstractDao() {
    super();
  }

  public List<T> findAll(Class<?> clazz) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select e from " + clazz.getName() + " e");

    return query.getResultList();
  }

  public void create(final T entity) {
    EntityManager em = EMF.getEM();
    em.persist(entity);
  }

  public void update(final T entity) {
    EntityManager em = EMF.getEM();
    em.merge(entity);
  }

  public void delete(final T entity) {
    EntityManager em = EMF.getEM();
    em.remove(entity);
  }

}