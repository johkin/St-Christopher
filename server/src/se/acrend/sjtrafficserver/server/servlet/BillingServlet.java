package se.acrend.sjtrafficserver.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.shared.model.PrepareBillingInfo;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import se.acrend.sjtrafficserver.server.service.impl.BillingServiceImpl;

public class BillingServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private BillingServiceImpl billingService = null;

  @Override
  public void init() throws ServletException {
    super.init();
    billingService = new BillingServiceImpl();
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    String action = req.getParameter("action");
    try {
      if ("getSubscription".equals(action)) {
        SubscriptionInfo result = billingService.getSubscription();

        JAXB.marshal(result, resp.getOutputStream());
      } else if ("getProductList".equals(action)) {
        ProductList result = billingService.getProductList();

        JAXB.marshal(result, resp.getOutputStream());
      } else if ("getMarketLicenseKey".equals(action)) {
        PrepareBillingInfo result = billingService.getMarketLicenseKey();

        JAXB.marshal(result, resp.getOutputStream());
      } else if ("billingCompleted".equals(action)) {
        String productId = req.getParameter("productId");

        SubscriptionInfo result = billingService.billingCompleted(productId);

        JAXB.marshal(result, resp.getOutputStream());
      }
    } catch (Exception e) {
      log.error("Kunde inte registrera bokning.", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
