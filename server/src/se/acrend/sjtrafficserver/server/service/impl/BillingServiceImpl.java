package se.acrend.sjtrafficserver.server.service.impl;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.ReturnCode;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import se.acrend.christopher.shared.model.ProductList.Product;
import se.acrend.sjtrafficserver.server.control.impl.SubscriptionControllerImpl;
import se.acrend.sjtrafficserver.server.dao.ProductDao;
import se.acrend.sjtrafficserver.server.dao.ServerDataDao;
import se.acrend.sjtrafficserver.server.dao.SubscriptionDao;
import se.acrend.sjtrafficserver.server.entity.ProductEntity;
import se.acrend.sjtrafficserver.server.entity.ProductEntity.ProductCategory;
import se.acrend.sjtrafficserver.server.entity.ProductEntity.ProductType;
import se.acrend.sjtrafficserver.server.entity.ServerDataEntity;
import se.acrend.sjtrafficserver.server.entity.SubscriptionEntity;
import se.acrend.sjtrafficserver.server.persistence.EMF;
import se.acrend.sjtrafficserver.server.util.DateUtil;

public class BillingServiceImpl {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final SubscriptionDao subscriptionDao;
  private final ServerDataDao serverDataDao;
  private final ProductDao productDao;

  private final SubscriptionControllerImpl subscriptionController;

  public BillingServiceImpl() {
    subscriptionDao = new SubscriptionDao();
    serverDataDao = new ServerDataDao();
    productDao = new ProductDao();

    subscriptionController = new SubscriptionControllerImpl();
  }

  public SubscriptionInfo getSubscription() {

    SubscriptionEntity subscription = subscriptionController.findSubscription();

    SubscriptionInfo result = new SubscriptionInfo();

    result.setNotificationCount(subscription.getNotificationCount());
    result.setTravelWarrantCount(subscription.getTravelWarrantCount());
    result.setNotificationExpireDate(subscription.getNotificationExpireDate());
    result.setTravelWarrantExpireDate(subscription.getTravelWarrantExpireDate());
    result.setReturnCode(ReturnCode.Success);

    EMF.close();
    return result;
  }

  public SubscriptionInfo billingCompleted(final String productId) {

    EntityManager em = EMF.getEM();

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

    EntityTransaction transaction = em.getTransaction();
    transaction.begin();

    subscriptionDao.update(subscription);

    result.setNotificationCount(subscription.getNotificationCount());
    result.setTravelWarrantCount(subscription.getTravelWarrantCount());
    result.setNotificationExpireDate(subscription.getNotificationExpireDate());
    result.setTravelWarrantExpireDate(subscription.getTravelWarrantExpireDate());
    result.setReturnCode(ReturnCode.Success);

    transaction.commit();

    EMF.close();
    return result;
  }

  public PrepareBillingInfo getMarketLicenseKey() {
    ServerDataEntity data = serverDataDao.findData();

    PrepareBillingInfo result = new PrepareBillingInfo();
    result.setMarketLicenseKey(data.getMarketLicenseKey());
    result.setReturnCode(ReturnCode.Success);

    EMF.close();

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
