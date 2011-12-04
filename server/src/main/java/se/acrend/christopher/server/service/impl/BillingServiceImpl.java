package se.acrend.christopher.server.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.control.impl.SubscriptionControllerImpl;
import se.acrend.christopher.server.dao.ProductDao;
import se.acrend.christopher.server.dao.ServerDataDao;
import se.acrend.christopher.server.entity.ProductEntity.ProductCategory;
import se.acrend.christopher.server.entity.ProductEntity.ProductType;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.ProductList.Product;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.SubscriptionInfo;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;

@Component
public class BillingServiceImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private SubscriptionControllerImpl subscriptionController;
  @Autowired
  private DatastoreService datastore;
  @Autowired
  private ProductDao productDao;
  @Autowired
  private ServerDataDao serverDataDao;

  public SubscriptionInfo getSubscription() {

    Entity subscription = subscriptionController.findSubscription();

    SubscriptionInfo result = new SubscriptionInfo();

    result.setNotificationCount(((Long) subscription.getProperty("notificationCount")).intValue());
    result.setTravelWarrantCount(((Long) subscription.getProperty("travelWarrantCount")).intValue());
    result.setNotificationExpireDate(DateUtil.toCalendar((Date) subscription.getProperty("notificationExpireDate")));
    result.setTravelWarrantExpireDate(DateUtil.toCalendar((Date) subscription.getProperty("travelWarrantExpireDate")));
    result.setReturnCode(ReturnCode.Success);

    return result;
  }

  public SubscriptionInfo billingCompleted(final String productId) {
    Entity subscription = subscriptionController.findSubscription();

    SubscriptionInfo result = new SubscriptionInfo();

    log.debug("Användare {} har köpt produkt {}.", new Object[] { subscription.getProperty("userEmail"), productId });
    Entity product = productDao.findByProductId(productId);
    if (product == null) {
      log.error("Hittade inte produkt med id {}", productId);
      result.setReturnCode(ReturnCode.Failure);

      return result;
    }
    ProductCategory category = ProductCategory.valueOf((String) product.getProperty("category"));
    ProductType type = ProductType.valueOf((String) product.getProperty("type"));
    String value = (String) product.getProperty("value");

    if (category == ProductCategory.Notification) {
      if (type == ProductType.Day) {
        Calendar now = DateUtil.createCalendar();
        Calendar expireDate = DateUtil.createCalendar();
        expireDate.setTime((Date) subscription.getProperty("notificationExpireDate"));

        if (expireDate.before(now)) {
          expireDate = now;
        }

        int days = Integer.parseInt(value);

        expireDate.add(Calendar.DAY_OF_YEAR, days);

        subscription.setProperty("notificationExpireDate", expireDate.getTime());
      }
      if (type == ProductType.Count) {

        long currentCount = (Long) subscription.getProperty("notificationCount");

        int count = Integer.parseInt(value);

        subscription.setProperty("notificationCount", currentCount + count);
      }
    }

    subscriptionController.update(subscription);

    result.setNotificationCount(((Long) subscription.getProperty("notificationCount")).intValue());
    result.setTravelWarrantCount(((Long) subscription.getProperty("travelWarrantCount")).intValue());
    result.setNotificationExpireDate(DateUtil.toCalendar((Date) subscription.getProperty("notificationExpireDate")));
    result.setTravelWarrantExpireDate(DateUtil.toCalendar((Date) subscription.getProperty("travelWarrantExpireDate")));

    result.setReturnCode(ReturnCode.Success);

    return result;
  }

  public PrepareBillingInfo getMarketLicenseKey() {
    PrepareBillingInfo result = new PrepareBillingInfo();

    Entity data = serverDataDao.findData();
    if (data.hasProperty("marketLicenseKey")) {
      result.setMarketLicenseKey((String) data.getProperty("marketLicenseKey"));
      result.setReturnCode(ReturnCode.Success);
    } else {
      log.error("Hittade inte server-konfiguration, lägg till Market License Key.");
      result.setReturnCode(ReturnCode.Failure);
    }

    return result;
  }

  public ProductList getProductList() {
    List<Entity> products = productDao.findProducts();

    ProductList result = new ProductList();

    for (Entity entity : products) {
      Product p = new Product();

      p.setDescription((String) entity.getProperty("description"));
      p.setName((String) entity.getProperty("name"));
      p.setProductId((String) entity.getProperty("productId"));

      result.addProduct(p);
    }

    result.setReturnCode(ReturnCode.Success);

    return result;
  }
}
