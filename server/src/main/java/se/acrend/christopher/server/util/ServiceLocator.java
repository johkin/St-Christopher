package se.acrend.christopher.server.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceLocator {

  private static BeanFactory beanFactory;

  public static <T> T getService(final Class<T> type) {
    return beanFactory.getBean(type);
  }

  @Autowired
  public void setBeanFactory(final BeanFactory beanFactory) {
    ServiceLocator.beanFactory = beanFactory;
  }

}
