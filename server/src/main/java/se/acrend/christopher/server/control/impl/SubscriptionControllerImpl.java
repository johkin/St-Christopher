package se.acrend.christopher.server.control.impl;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.dao.SubscriptionDao;
import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.util.DateUtil;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

@Component
public class SubscriptionControllerImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private SubscriptionDao subscriptionDao;
  @Autowired
  private UserService userService;
  @Autowired
  private DatastoreService datastore;

  public Entity findSubscription() {
    User user = userService.getCurrentUser();

    Entity subscription = subscriptionDao.findByUser(user.getEmail());

    if (subscription == null) {
      Transaction transaction = datastore.beginTransaction();

      subscription = new Entity(DataConstants.KIND_SUBSCRIPTION);
      // TODO Long?
      subscription.setProperty("notificationCount", 5);
      subscription.setProperty("travelWarrantCount", 5);

      Calendar calendar = DateUtil.createCalendar();
      calendar.add(Calendar.DAY_OF_YEAR, -1);
      calendar.set(Calendar.HOUR, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      subscription.setProperty("notificationExpireDate", calendar.getTime());
      subscription.setProperty("travelWarrantExpireDate", calendar.getTime());
      subscription.setProperty("userEmail", user.getEmail());
      datastore.put(subscription);

      transaction.commit();
    }

    return subscription;
  }

  public void update(final Entity subscription) {
    Transaction transaction = datastore.beginTransaction();
    datastore.put(subscription);
    transaction.commit();
  }

}
