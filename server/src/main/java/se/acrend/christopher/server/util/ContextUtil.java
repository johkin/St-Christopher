package se.acrend.christopher.server.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContextUtil {

  private static BeanFactory beanFactory;

  public static <T> T getBean(final Class<T> type) {
    return beanFactory.getBean(type);
  }

  @Autowired
  public void setBeanFactory(final BeanFactory beanFactory) {
    ContextUtil.beanFactory = beanFactory;
  }

}
