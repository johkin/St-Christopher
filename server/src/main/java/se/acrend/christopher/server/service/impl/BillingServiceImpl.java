package se.acrend.christopher.server.service.impl;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.acrend.christopher.server.control.impl.SubscriptionControllerImpl;
import se.acrend.christopher.server.dao.ProductDao;
import se.acrend.christopher.server.dao.ServerDataDao;
import se.acrend.christopher.server.dao.SubscriptionDao;
import se.acrend.christopher.server.entity.ProductEntity;
import se.acrend.christopher.server.entity.ProductEntity.ProductCategory;
import se.acrend.christopher.server.entity.ProductEntity.ProductType;
import se.acrend.christopher.server.entity.ServerDataEntity;
import se.acrend.christopher.server.entity.SubscriptionEntity;
import se.acrend.christopher.server.util.DateUtil;
import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.ProductList.Product;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.SubscriptionInfo;

@Component
public class BillingServiceImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private SubscriptionDao subscriptionDao;
  @Autowired
  private ServerDataDao serverDataDao;
  @Autowired
  private ProductDao productDao;
  @Autowired
  private SubscriptionControllerImpl subscriptionController;

  public SubscriptionInfo getSubscription() {

    SubscriptionEntity subscription = subscriptionController.findSubscription();

    SubscriptionInfo result = new SubscriptionInfo();

    result.setNotificationCount(subscription.getNotificationCount());
    result.setTravelWarrantCount(subscription.getTravelWarrantCount());
    result.setNotificationExpireDate(DateUtil.toCalendar(subscription.getNotificationExpireDate()));
    result.setTravelWarrantExpireDate(DateUtil.toCalendar(subscription.getTravelWarrantExpireDate()));
    result.setReturnCode(ReturnCode.Success);

    return result;
  }

  public SubscriptionInfo billingCompleted(final String productId) {
    SubscriptionEntity subscription = subscriptionController.findSubscription();

    SubscriptionInfo result = new SubscriptionInfo();

    log.debug("Användare {} har köpt produkt {}.", new Object[] { subscription.getUserEmail(), productId });

    ProductEntity product = productDao.findByProductId(productId);

    if (product == null) {
      result.setReturnCode(ReturnCode.Failure);

      return result;
    }

    if (ProductCategory.valueOf(product.getCategory()) == ProductCategory.Notification) {
      if (ProductType.valueOf(product.getType()) == ProductType.Day) {
        Calendar now = DateUtil.createCalendar();
        Calendar expireDate = DateUtil.createCalendar();
        expireDate.setTime(subscription.getNotificationExpireDate());

        if (expireDate.before(now)) {
          expireDate = now;
        }

        int days = Integer.parseInt(product.getValue());

        expireDate.add(Calendar.DAY_OF_YEAR, days);

        subscription.setNotificationExpireDate(expireDate.getTime());
      }
      if (ProductType.valueOf(product.getType()) == ProductType.Count) {

        int currentCount = subscription.getNotificationCount();

        int count = Integer.parseInt(product.getValue());

        subscription.setNotificationCount(currentCount + count);
      }
    }

    subscriptionDao.update(subscription);

    result.setNotificationCount(subscription.getNotificationCount());
    result.setTravelWarrantCount(subscription.getTravelWarrantCount());
    result.setNotificationExpireDate(DateUtil.toCalendar(subscription.getNotificationExpireDate()));
    result.setTravelWarrantExpireDate(DateUtil.toCalendar(subscription.getTravelWarrantExpireDate()));
    result.setReturnCode(ReturnCode.Success);

    return result;
  }

  public PrepareBillingInfo getMarketLicenseKey() {
    ServerDataEntity data = serverDataDao.findData();

    PrepareBillingInfo result = new PrepareBillingInfo();
    result.setMarketLicenseKey(data.getMarketLicenseKey());
    result.setReturnCode(ReturnCode.Success);

    return result;
  }

  public ProductList getProductList() {
    List<ProductEntity> products = productDao.findAll(ProductEntity.class);

    ProductList result = new ProductList();

    for (ProductEntity entity : products) {
      Product p = new Product();

      p.setDescription(entity.getDescription());
      p.setName(entity.getName());
      p.setProductId(entity.getProductId());

      result.addProduct(p);
    }

    result.setReturnCode(ReturnCode.Success);

    return result;
  }
}
