package se.acrend.christopher.server.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.stereotype.Repository;

import se.acrend.christopher.server.entity.TrainStopEntity;
import se.acrend.christopher.server.util.DateUtil;

@Repository
public class TrainStopDao extends AbstractDao<TrainStopEntity> {

  public List<TrainStopEntity> findByTrainNo(final String trainNo, final Calendar date) {
    return operations.find("select t from " + TrainStopEntity.class.getName()
        + " t where trainNo = :trainNo and date = :date", trainNo, DateUtil.toDate(date));
  }

  public List<TrainStopEntity> findDepartures(final Calendar from, final Calendar to) {
    return operations.find("select t from " + TrainStopEntity.class.getName()
        + " t where originalDeparture > :from and originalDeparture < :to", DateUtil.toDate(from), DateUtil.toDate(to));
  }

  public List<TrainStopEntity> findArrivalsNotArrived(final Calendar from, final Calendar to) {
    return operations
        .find(
            "select t from "
                + TrainStopEntity.class.getName()
                + " t where originalArrival > :from and originalArrival < :to and actualArrival is null and originalArrival is not null",
            DateUtil.toDate(from), DateUtil.toDate(to));
  }

  public int deleteOldEntries(final Calendar time) {
    return operations.execute(new JpaCallback<Integer>() {

      @Override
      public Integer doInJpa(final EntityManager em) throws PersistenceException {
        Query query = em.createQuery("delete from " + TrainStopEntity.class.getName() + " t where date < :time");
        query.setParameter("time", DateUtil.toDate(time));

        return query.executeUpdate();
      }

    });
  }
}
