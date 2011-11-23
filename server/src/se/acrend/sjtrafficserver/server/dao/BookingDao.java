package se.acrend.sjtrafficserver.server.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.entity.BookingEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.appengine.api.datastore.Key;

public class BookingDao extends AbstractDao<BookingEntity> {

  public BookingEntity findByKey(final Key key) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName() + " b where key = :key");
    query.setParameter("key", key);

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }

    return (BookingEntity) resultList.get(0);
  }

  public List<BookingEntity> findBookings() {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName() + " b");

    return query.getResultList();
  }

  public List<BookingEntity> findBookings(final String email) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName() + " b where userEmail = :userEmail");
    query.setParameter("userEmail", email);

    return query.getResultList();
  }

  public BookingEntity findByTrainNoDateUser(final String trainNo, final Calendar date, final String email) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName()
        + " b where trainNo = :trainNo and date = :date and userEmail = :userEmail");
    query.setParameter("trainNo", trainNo);
    query.setParameter("userEmail", email);
    query.setParameter("date", DateUtil.toDate(date));

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }
    return (BookingEntity) resultList.get(0);
  }

  public BookingEntity findByCodeUser(final String code, final String email) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName()
        + " b where code = :code and userEmail = :userEmail");
    query.setParameter("code", code);
    query.setParameter("userEmail", email);

    List resultList = query.getResultList();
    if (resultList.isEmpty()) {
      return null;
    }
    return (BookingEntity) resultList.get(0);
  }

  public List<BookingEntity> findByDeparture(final Key key) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName() + " b where departure = :departure");
    query.setParameter("departure", key);

    return query.getResultList();
  }

  public List<BookingEntity> findByArrival(final Key key) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select b from " + BookingEntity.class.getName() + " b where arrival = :arrival");
    query.setParameter("arrival", key);

    return query.getResultList();
  }

  public int deleteOldEntries(final Calendar time) {

    EntityManager em = EMF.getEM();

    Query query = em
        .createQuery("delete from "
            + BookingEntity.class.getName()
            + " t where date < :time");
    query.setParameter("time", DateUtil.toDate(time));

    return query.executeUpdate();

  }
}
