package se.acrend.sjtrafficserver.server.control.impl;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.sjtrafficserver.server.dao.SubscriptionDao;
import se.acrend.sjtrafficserver.server.entity.SubscriptionEntity;
import se.acrend.sjtrafficserver.server.util.DateUtil;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class SubscriptionControllerImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final SubscriptionDao subscriptionDao;

  private UserService userService = null;

  public SubscriptionControllerImpl() {
    subscriptionDao = new SubscriptionDao();

    userService = UserServiceFactory.getUserService();
  }

  public SubscriptionEntity findSubscription() {
    User user = userService.getCurrentUser();

    SubscriptionEntity subscription = subscriptionDao.findByUser(user.getEmail());

    if (subscription == null) {
      subscription = new SubscriptionEntity();
      subscription.setNotificationCount(5);
      subscription.setTravelWarrantCount(5);

      Calendar calendar = DateUtil.createCalendar();
      calendar.add(Calendar.DAY_OF_YEAR, -1);
      calendar.set(Calendar.HOUR, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      subscription.setNotificationExpireDate(calendar.getTime());
      subscription.setTravelWarrantExpireDate(calendar.getTime());
      subscription.setUserEmail(user.getEmail());
      subscriptionDao.create(subscription);
    }

    return subscription;
  }

  public void update(final SubscriptionEntity subscription) {
    subscriptionDao.update(subscription);
  }

}
