package se.acrend.christopher.android.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.billing.Consts.PurchaseState;
import se.acrend.christopher.android.billing.Consts.ResponseCode;
import se.acrend.christopher.android.billing.PurchaseObserver;
import se.acrend.christopher.android.billing.ResponseHandler;
import se.acrend.christopher.android.service.BillingService;
import se.acrend.christopher.android.service.BillingService.RequestPurchase;
import se.acrend.christopher.android.util.DateUtil;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.ProductList.Product;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

public class SubscriptionDetails extends RoboActivity {

  private static final String TAG = "SubscriptionDetails";

  @InjectView(R.id.subscription_details_info)
  private TextView detailsInfo;

  @InjectView(R.id.subscription_details_buy)
  private Button buyButton;
  @InjectView(R.id.subscription_details_productList)
  private Spinner productList;

  @Inject
  private Context context;

  @Inject
  private BillingService billingService;

  private ProgressDialog dialog;

  private Handler handler;

  private SubscriptionPurchaseObserver purchaseObserver;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.subscription_details);

    detailsInfo.setText("");

    startService(new Intent(context, BillingService.class));

    buyButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        billingService.requestPurchase("test1", null);
      }
    });

    handler = new Handler();
    purchaseObserver = new SubscriptionPurchaseObserver(handler);

    // Check if billing is supported.
    ResponseHandler.register(purchaseObserver);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!billingService.checkBillingSupported()) {
      // TODO showDialog(DIALOG_CANNOT_CONNECT_ID);
    }

    final String message = context.getResources().getString(R.string.subscription_details_subscriptionloading);

    final AsyncTask<Void, Void, ProductList> getProductTask = new AsyncTask<Void, Void, ProductList>() {

      @Override
      protected void onPreExecute() {
        dialog.setMessage(context.getResources().getString(R.string.subscription_details_productsloading));
      }

      @Override
      protected ProductList doInBackground(final Void... params) {
        return billingService.getProductList();
      }

      @Override
      protected void onCancelled() {
        dialog.dismiss();
      }

      @Override
      protected void onPostExecute(final ProductList result) {
        ArrayAdapter<Product> s = new ArrayAdapter<ProductList.Product>(context, R.layout.calendars_dropdown_item,
            result.getProducts()) {

          @Override
          public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {

            Product product = getItem(position);

            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.product_dropdown_item, parent, false);

            TextView name = (TextView) v.findViewById(R.id.name);
            TextView description = (TextView) v.findViewById(R.id.description);

            name.setText(product.getName());
            description.setText(product.getDescription());

            return v;
          }

          @Override
          public View getView(final int position, final View convertView, final ViewGroup parent) {
            Product product = getItem(position);

            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.product_item, parent, false);

            TextView name = (TextView) v.findViewById(R.id.name);

            name.setText(product.getName());

            return v;
          }

        };
        productList.setAdapter(s);

        dialog.dismiss();
      }
    };

    final AsyncTask<Void, Void, SubscriptionInfo> getSubscriptionTask = new AsyncTask<Void, Void, SubscriptionInfo>() {

      @Override
      protected SubscriptionInfo doInBackground(final Void... params) {
        return billingService.getSubscriptionInfo();
      }

      @Override
      protected void onCancelled() {
        dialog.dismiss();
      }

      @Override
      protected void onPostExecute(final SubscriptionInfo result) {
        String info = context.getString(R.string.subscription_details_info,
            DateUtil.formatTime(result.getNotificationExpireDate()), result.getNotificationCount());
        detailsInfo.setText(info);

        getProductTask.execute();
      }
    };
    dialog = ProgressDialog.show(SubscriptionDetails.this, "", message, true, true,
        new DialogInterface.OnCancelListener() {

          @Override
          public void onCancel(final DialogInterface dialog) {
            getSubscriptionTask.cancel(false);
            getProductTask.cancel(false);
            finish();
          }
        });
    getSubscriptionTask.execute();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (dialog.isShowing()) {
      dialog.cancel();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    billingService.unbind();
    stopService(new Intent(context, BillingService.class));
  }

  /**
   * A {@link PurchaseObserver} is used to get callbacks when Android Market
   * sends messages to this application so that we can update the UI.
   */
  private class SubscriptionPurchaseObserver extends PurchaseObserver {
    public SubscriptionPurchaseObserver(final Handler handler) {
      super(SubscriptionDetails.this, handler);
    }

    @Override
    public void onBillingSupported(final boolean supported) {
      Log.i(TAG, "supported: " + supported);
      if (supported) {
        buyButton.setEnabled(true);
      } else {
        // showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
      }
    }

    @Override
    public void onPurchaseStateChange(final PurchaseState purchaseState, final String itemId, final long purchaseTime,
        final String developerPayload) {
      Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
      if (purchaseState == PurchaseState.PURCHASED) {
        // TODO Uppdatera produkter
        // TODO Schemalägg registrering av kommande resor?
      }
    }

    @Override
    public void onRequestPurchaseResponse(final RequestPurchase request, final ResponseCode responseCode) {
      Log.d(TAG, request.mProductId + ": " + responseCode);
      if (responseCode == ResponseCode.RESULT_OK) {
        Log.i(TAG, "purchase was successfully sent to server");
      } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
        Log.i(TAG, "user canceled purchase");
      } else {
        Log.i(TAG, "purchase failed");
      }
    }

  }
}
