package se.acrend.sjtrafficserver.server.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import se.acrend.sjtrafficserver.server.entity.TrainStopEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class TrainStopDao extends AbstractDao<TrainStopEntity> {

  public List<TrainStopEntity> findByTrainNo(final String trainNo, final Calendar date) {
    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select t from " + TrainStopEntity.class.getName()
        + " t where trainNo = :trainNo and date = :date");
    query.setParameter("trainNo", trainNo);
    query.setParameter("date", DateUtil.toDate(date));

    return query.getResultList();
  }

  public List<TrainStopEntity> findDepartures(final Calendar from, final Calendar to) {

    EntityManager em = EMF.getEM();

    Query query = em.createQuery("select t from " + TrainStopEntity.class.getName() + " t "
        + "where originalDeparture > :from and originalDeparture < :to");
    query.setParameter("from", DateUtil.toDate(from));
    query.setParameter("to", DateUtil.toDate(to));

    return query.getResultList();
  }

  public List<TrainStopEntity> findArrivalsNotArrived(final Calendar from, final Calendar to) {

    EntityManager em = EMF.getEM();

    Query query = em
        .createQuery("select t from "
            + TrainStopEntity.class.getName()
            + " t where originalArrival > :from and originalArrival < :to and actualArrival is null and originalArrival is not null");
    query.setParameter("from", DateUtil.toDate(from));
    query.setParameter("to", DateUtil.toDate(to));

    return query.getResultList();
  }

  public int deleteOldEntries(final Calendar time) {

    EntityManager em = EMF.getEM();

    Query query = em
        .createQuery("delete from "
            + TrainStopEntity.class.getName()
            + " t where date < :time");
    query.setParameter("time", DateUtil.toDate(time));

    return query.executeUpdate();

  }

}
