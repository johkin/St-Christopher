package se.acrend.christopher.server.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.stereotype.Repository;

import se.acrend.christopher.server.entity.BookingEntity;
import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.Key;

@Repository
public class BookingDao extends AbstractDao<BookingEntity> {

  public BookingEntity findByKey(final Key key) {

    List resultList = operations.find("select b from " + BookingEntity.class.getName() + " b where key = :key", key);

    if (resultList.isEmpty()) {
      return null;
    }

    return (BookingEntity) resultList.get(0);
  }

  public List<BookingEntity> findBookings() {

    return operations.find("select b from " + BookingEntity.class.getName() + " b");
  }

  public List<BookingEntity> findBookings(final String email) {

    return operations.find("select b from " + BookingEntity.class.getName() + " b where userEmail = :userEmail", email);
  }

  public BookingEntity findByTrainNoDateUser(final String trainNo, final Calendar date, final String email) {

    List resultList = operations.find("select b from " + BookingEntity.class.getName()
        + " b where trainNo = :trainNo and date = :date and userEmail = :userEmail", trainNo, email,
        DateUtil.toDate(date));

    if (resultList.isEmpty()) {
      return null;
    }
    return (BookingEntity) resultList.get(0);
  }

  public BookingEntity findByCodeUser(final String code, final String email) {

    List resultList = operations.find("select b from " + BookingEntity.class.getName()
        + " b where code = :code and userEmail = :userEmail", code, email);

    if (resultList.isEmpty()) {
      return null;
    }
    return (BookingEntity) resultList.get(0);
  }

  public List<BookingEntity> findByDeparture(final Key key) {

    return operations.find("select b from " + BookingEntity.class.getName() + " b where departure = :departure", key);
  }

  public List<BookingEntity> findByArrival(final Key key) {

    return operations.find("select b from " + BookingEntity.class.getName() + " b where arrival = :arrival", key);
  }

  public int deleteOldEntries(final Calendar time) {
    return operations.execute(new JpaCallback<Integer>() {

      @Override
      public Integer doInJpa(final EntityManager em) throws PersistenceException {
        Query query = em.createQuery("delete from " + BookingEntity.class.getName() + " t where date < :time");
        query.setParameter("time", DateUtil.toDate(time));

        return query.executeUpdate();
      }
    });

  }
}
