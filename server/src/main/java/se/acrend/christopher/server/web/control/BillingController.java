package se.acrend.christopher.server.web.control;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.acrend.christopher.server.service.impl.BillingServiceImpl;
import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.SubscriptionInfo;

@Controller
public class BillingController {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  private final BillingServiceImpl billingService = null;

  @RequestMapping(value = "/billing/getSubScription")
  public void getSubscription(final HttpServletResponse response) throws IOException {
    try {
      SubscriptionInfo result = billingService.getSubscription();

      JAXB.marshal(result, response.getOutputStream());
    } catch (Exception e) {
      log.error("Kunde inte hämta prenumeration.", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/billing/getProductList")
  public void getProductList(final HttpServletResponse response) throws IOException {
    try {
      ProductList result = billingService.getProductList();

      JAXB.marshal(result, response.getOutputStream());
    } catch (Exception e) {
      log.error("Kunde inte hämta prenumeration.", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/billing/getMarketLicenseKey")
  public void getMarketLicenseKey(final HttpServletResponse response) throws IOException {
    try {
      PrepareBillingInfo result = billingService.getMarketLicenseKey();

      JAXB.marshal(result, response.getOutputStream());
    } catch (Exception e) {
      log.error("Kunde inte hämta prenumeration.", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/billing/billingCompleted")
  public void billingCompleted(@RequestParam final String productId, final HttpServletResponse response)
      throws IOException {
    try {
      SubscriptionInfo result = billingService.billingCompleted(productId);

      JAXB.marshal(result, response.getOutputStream());
    } catch (Exception e) {
      log.error("Kunde inte hämta prenumeration.", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
