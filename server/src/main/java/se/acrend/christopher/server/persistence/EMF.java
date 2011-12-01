package se.acrend.christopher.server.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component("entityManagerFactory")
public class EMF implements FactoryBean<EntityManagerFactory> {

  private static final Logger log = LoggerFactory.getLogger(EMF.class);

  private static final EntityManagerFactory emfInstance = Persistence
      .createEntityManagerFactory("transactions-optional");

  @Override
  public EntityManagerFactory getObject() throws Exception {
    return emfInstance;
  }

  @Override
  public Class<?> getObjectType() {
    return EntityManagerFactory.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
