package se.acrend.christopher.android.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import roboguice.inject.ContextScoped;
import roboguice.service.RoboService;
import se.acrend.christopher.android.billing.Consts;
import se.acrend.christopher.android.billing.Consts.PurchaseState;
import se.acrend.christopher.android.billing.Consts.ResponseCode;
import se.acrend.christopher.android.billing.ResponseHandler;
import se.acrend.christopher.android.billing.Security;
import se.acrend.christopher.android.billing.Security.VerifiedPurchase;
import se.acrend.christopher.android.receiver.BillingReceiver;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.SubscriptionInfo;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.google.inject.Inject;

/**
 * This class sends messages to Android Market on behalf of the application by
 * connecting (binding) to the MarketBillingService. The application creates an
 * instance of this class and invokes billing requests through this service. The
 * {@link BillingReceiver} class starts this service to process commands that it
 * receives from Android Market. You should modify and obfuscate this code
 * before using it.
 */
@ContextScoped
public class BillingService extends RoboService implements ServiceConnection {
  private static final String TAG = "BillingService";

  /** The service connection to the remote MarketBillingService. */
  private static IMarketBillingService mService;

  /**
   * The list of requests that are pending while we are waiting for the
   * connection to the MarketBillingService to be established.
   */
  private static LinkedList<BillingRequest> mPendingRequests = new LinkedList<BillingRequest>();

  /**
   * The list of requests that we have sent to Android Market but for which we
   * have not yet received a response code. The HashMap is indexed by the
   * request Id that each request receives when it executes.
   */
  private static HashMap<Long, BillingRequest> mSentRequests = new HashMap<Long, BillingRequest>();

  @Inject
  private BillingHelper billingHelper;

  @Inject
  private Context context;

  private boolean baseContextSet = false;

  /**
   * The base class for all requests that use the MarketBillingService. Each
   * derived class overrides the run() method to call the appropriate service
   * interface. If we are already connected to the MarketBillingService, then we
   * call the run() method directly. Otherwise, we bind to the service and save
   * the request on a queue to be run later when the service is connected.
   */
  abstract class BillingRequest {
    private final int mStartId;
    protected long mRequestId;

    public BillingRequest(final int startId) {
      mStartId = startId;
    }

    public int getStartId() {
      return mStartId;
    }

    /**
     * Run the request, starting the connection if necessary.
     * 
     * @return true if the request was executed or queued; false if there was an
     *         error starting the connection
     */
    public boolean runRequest() {
      if (runIfConnected()) {
        return true;
      }

      if (bindToMarketBillingService()) {
        // Add a pending request to run when the service is connected.
        mPendingRequests.add(this);
        return true;
      }
      return false;
    }

    /**
     * Try running the request directly if the service is already connected.
     * 
     * @return true if the request ran successfully; false if the service is not
     *         connected or there was an error when trying to use it
     */
    public boolean runIfConnected() {
      initContext();
      Log.d(TAG, getClass().getSimpleName());
      if (mService != null) {
        try {
          mRequestId = run();
          Log.d(TAG, "request id: " + mRequestId);
          if (mRequestId >= 0) {
            mSentRequests.put(mRequestId, this);
          }
          return true;
        } catch (RemoteException e) {
          onRemoteException(e);
        }
      }
      return false;
    }

    /**
     * Called when a remote exception occurs while trying to execute the
     * {@link #run()} method. The derived class can override this to execute
     * exception-handling code.
     * 
     * @param e
     *          the exception
     */
    protected void onRemoteException(final RemoteException e) {
      Log.w(TAG, "remote billing service crashed");
      mService = null;
    }

    /**
     * The derived class must implement this method.
     * 
     * @throws RemoteException
     */
    abstract protected long run() throws RemoteException;

    /**
     * This is called when Android Market sends a response code for this
     * request.
     * 
     * @param responseCode
     *          the response code
     */
    protected void responseCodeReceived(final ResponseCode responseCode) {
    }

    protected Bundle makeRequestBundle(final String method) {
      Bundle request = new Bundle();
      request.putString(Consts.BILLING_REQUEST_METHOD, method);
      request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
      request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
      return request;
    }

    protected void logResponseCode(final String method, final Bundle response) {
      ResponseCode responseCode = ResponseCode.valueOf(response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE));
      Log.d(TAG, method + " received " + responseCode.toString());
    }
  }

  /**
   * Wrapper class that checks if in-app billing is supported.
   */
  public class CheckBillingSupported extends BillingRequest {
    public CheckBillingSupported() {
      // This object is never created as a side effect of starting this
      // service so we pass -1 as the startId to indicate that we should
      // not stop this service after executing this request.
      super(-1);
    }

    @Override
    protected long run() throws RemoteException {
      Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
      Bundle response = mService.sendBillingRequest(request);
      int responseCode = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
      Log.i(TAG, "CheckBillingSupported response code: " + ResponseCode.valueOf(responseCode));
      boolean billingSupported = (responseCode == ResponseCode.RESULT_OK.ordinal());
      ResponseHandler.checkBillingSupportedResponse(billingSupported);
      return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
    }
  }

  /**
   * Wrapper class that requests a purchase.
   */
  public class RequestPurchase extends BillingRequest {
    public final String mProductId;
    public final String mDeveloperPayload;

    public RequestPurchase(final String itemId) {
      this(itemId, null);
    }

    public RequestPurchase(final String itemId, final String developerPayload) {
      // This object is never created as a side effect of starting this
      // service so we pass -1 as the startId to indicate that we should
      // not stop this service after executing this request.
      super(-1);
      mProductId = itemId;
      mDeveloperPayload = developerPayload;
    }

    @Override
    protected long run() throws RemoteException {
      Bundle request = makeRequestBundle("REQUEST_PURCHASE");
      request.putString(Consts.BILLING_REQUEST_ITEM_ID, mProductId);
      // Note that the developer payload is optional.
      if (mDeveloperPayload != null) {
        request.putString(Consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, mDeveloperPayload);
      }
      Bundle response = mService.sendBillingRequest(request);
      PendingIntent pendingIntent = response.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
      if (pendingIntent == null) {
        Log.e(TAG, "Error with requestPurchase");
        return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
      }

      Intent intent = new Intent();
      ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
      return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }

    @Override
    protected void responseCodeReceived(final ResponseCode responseCode) {
      ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
    }
  }

  /**
   * Wrapper class that confirms a list of notifications to the server.
   */
  public class ConfirmNotifications extends BillingRequest {
    final String[] mNotifyIds;

    public ConfirmNotifications(final int startId, final String[] notifyIds) {
      super(startId);
      mNotifyIds = notifyIds;
    }

    @Override
    protected long run() throws RemoteException {
      Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
      request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
      Bundle response = mService.sendBillingRequest(request);
      logResponseCode("confirmNotifications", response);
      return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }
  }

  /**
   * Wrapper class that sends a GET_PURCHASE_INFORMATION message to the server.
   */
  public class GetPurchaseInformation extends BillingRequest {
    long mNonce;
    final String[] mNotifyIds;

    public GetPurchaseInformation(final int startId, final String[] notifyIds) {
      super(startId);
      mNotifyIds = notifyIds;
    }

    @Override
    protected long run() throws RemoteException {
      String key = billingHelper.getMarketLicenseKey();
      mNonce = Security.generateNonce();
      Security.setBase64EncodedPublicKey(key);

      Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
      request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
      request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
      Bundle response = mService.sendBillingRequest(request);
      logResponseCode("getPurchaseInformation", response);
      return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }

    @Override
    protected void onRemoteException(final RemoteException e) {
      super.onRemoteException(e);
      Security.removeNonce(mNonce);
    }
  }

  /**
   * We don't support binding to this service, only starting the service.
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    Log.d(TAG, "onCreate");
    super.onCreate();
    initContext();
  }

  @Override
  public void onStart(final Intent intent, final int startId) {
    handleCommand(intent, startId);
  }

  /**
   * The {@link BillingReceiver} sends messages to this service using intents.
   * Each intent has an action and some extra arguments specific to that action.
   * 
   * @param intent
   *          the intent containing one of the supported actions
   * @param startId
   *          an identifier for the invocation instance of this service
   */
  public void handleCommand(final Intent intent, final int startId) {
    String action = intent.getAction();
    Log.i(TAG, "handleCommand() action: " + action);
    if (Consts.ACTION_CONFIRM_NOTIFICATION.equals(action)) {
      String[] notifyIds = intent.getStringArrayExtra(Consts.NOTIFICATION_ID);
      confirmNotifications(startId, notifyIds);
    } else if (Consts.ACTION_GET_PURCHASE_INFORMATION.equals(action)) {
      String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
      getPurchaseInformation(startId, new String[] { notifyId });
    } else if (Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
      String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
      String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);
      purchaseStateChanged(startId, signedData, signature);
    } else if (Consts.ACTION_RESPONSE_CODE.equals(action)) {
      long requestId = intent.getLongExtra(Consts.INAPP_REQUEST_ID, -1);
      int responseCodeIndex = intent.getIntExtra(Consts.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
      ResponseCode responseCode = ResponseCode.valueOf(responseCodeIndex);
      checkResponseCode(requestId, responseCode);
    }
  }

  /**
   * Binds to the MarketBillingService and returns true if the bind succeeded.
   * 
   * @return true if the bind succeeded; false otherwise
   */
  private boolean bindToMarketBillingService() {
    initContext();
    try {
      Log.i(TAG, "binding to Market billing service");
      boolean bindResult = bindService(new Intent(Consts.MARKET_BILLING_SERVICE_ACTION), this, // ServiceConnection.
          Context.BIND_AUTO_CREATE);

      if (bindResult) {
        return true;
      } else {
        Log.e(TAG, "Could not bind to service.");
      }
    } catch (SecurityException e) {
      Log.e(TAG, "Security exception: " + e);
    }
    return false;
  }

  void initContext() {
    Log.d(TAG, "baseContext is set: " + baseContextSet);
    if (!baseContextSet) {
      Log.d(TAG, "current context: " + getBaseContext());
      if (getBaseContext() == null) {
        Log.d(TAG, "attaching context: " + context);
        attachBaseContext(context);
      }
      baseContextSet = true;
    }
  }

  public SubscriptionInfo getSubscriptionInfo() {
    Log.d(TAG, "Hämta prenumeration");

    SubscriptionInfo information = billingHelper.getSubscriptionInfo();

    return information;
  }

  public ProductList getProductList() {
    Log.d(TAG, "Hämta produkter");

    ProductList information = billingHelper.getProductList();

    return information;
  }

  /**
   * Checks if in-app billing is supported.
   * 
   * @return true if supported; false otherwise
   */
  public boolean checkBillingSupported() {
    return new CheckBillingSupported().runRequest();
  }

  /**
   * Requests that the given item be offered to the user for purchase. When the
   * purchase succeeds (or is canceled) the {@link BillingReceiver} receives an
   * intent with the action {@link Consts#ACTION_NOTIFY}. Returns false if there
   * was an error trying to connect to Android Market.
   * 
   * @param productId
   *          an identifier for the item being offered for purchase
   * @param developerPayload
   *          a payload that is associated with a given purchase, if null, no
   *          payload is sent
   * @return false if there was an error connecting to Android Market
   */
  public boolean requestPurchase(final String productId, final String developerPayload) {
    return new RequestPurchase(productId, developerPayload).runRequest();
  }

  /**
   * Confirms receipt of a purchase state change. Each {@code notifyId} is an
   * opaque identifier that came from the server. This method sends those
   * identifiers back to the MarketBillingService, which ACKs them to the
   * server. Returns false if there was an error trying to connect to the
   * MarketBillingService.
   * 
   * @param startId
   *          an identifier for the invocation instance of this service
   * @param notifyIds
   *          a list of opaque identifiers associated with purchase state
   *          changes.
   * @return false if there was an error connecting to Market
   */
  private boolean confirmNotifications(final int startId, final String[] notifyIds) {
    return new ConfirmNotifications(startId, notifyIds).runRequest();
  }

  /**
   * Gets the purchase information. This message includes a list of notification
   * IDs sent to us by Android Market, which we include in our request. The
   * server responds with the purchase information, encoded as a JSON string,
   * and sends that to the {@link BillingReceiver} in an intent with the action
   * {@link Consts#ACTION_PURCHASE_STATE_CHANGED}. Returns false if there was an
   * error trying to connect to the MarketBillingService.
   * 
   * @param startId
   *          an identifier for the invocation instance of this service
   * @param notifyIds
   *          a list of opaque identifiers associated with purchase state
   *          changes
   * @return false if there was an error connecting to Android Market
   */
  private boolean getPurchaseInformation(final int startId, final String[] notifyIds) {
    return new GetPurchaseInformation(startId, notifyIds).runRequest();
  }

  /**
   * Verifies that the data was signed with the given signature, and calls
   * {@link ResponseHandler#purchaseResponse(Context, PurchaseState, String, String, long)}
   * for each verified purchase.
   * 
   * @param startId
   *          an identifier for the invocation instance of this service
   * @param signedData
   *          the signed JSON string (signed, not encrypted)
   * @param signature
   *          the signature for the data, signed with the private key
   */
  private void purchaseStateChanged(final int startId, final String signedData, final String signature) {
    ArrayList<Security.VerifiedPurchase> purchases;
    if (Security.getBase64EncodedPublicKey() == null) {
      String key = billingHelper.getMarketLicenseKey();
      Security.setBase64EncodedPublicKey(key);
    }
    purchases = Security.verifyPurchase(signedData, signature);
    if (purchases == null) {
      return;
    }

    ArrayList<String> notifyList = new ArrayList<String>();
    for (VerifiedPurchase vp : purchases) {
      if (vp.notificationId != null) {
        notifyList.add(vp.notificationId);
      }
      ResponseHandler.purchaseResponse(this, vp.purchaseState, vp.productId, vp.orderId, vp.purchaseTime,
          vp.developerPayload);
      billingHelper.sendBillingCompleted(vp.productId, vp.nonce);
    }
    if (!notifyList.isEmpty()) {
      String[] notifyIds = notifyList.toArray(new String[notifyList.size()]);
      confirmNotifications(startId, notifyIds);
    }
  }

  /**
   * This is called when we receive a response code from Android Market for a
   * request that we made. This is used for reporting various errors and for
   * acknowledging that an order was sent to the server. This is NOT used for
   * any purchase state changes. All purchase state changes are received in the
   * {@link BillingReceiver} and passed to this service, where they are handled
   * in {@link #purchaseStateChanged(int, String, String)}.
   * 
   * @param requestId
   *          a number that identifies a request, assigned at the time the
   *          request was made to Android Market
   * @param responseCode
   *          a response code from Android Market to indicate the state of the
   *          request
   */
  private void checkResponseCode(final long requestId, final ResponseCode responseCode) {
    BillingRequest request = mSentRequests.get(requestId);
    if (request != null) {
      Log.d(TAG, request.getClass().getSimpleName() + ": " + responseCode);
      request.responseCodeReceived(responseCode);
    }
    mSentRequests.remove(requestId);
  }

  /**
   * Runs any pending requests that are waiting for a connection to the service
   * to be established. This runs in the main UI thread.
   */
  private void runPendingRequests() {
    int maxStartId = -1;
    BillingRequest request;
    while ((request = mPendingRequests.peek()) != null) {
      if (request.runIfConnected()) {
        // Remove the request
        mPendingRequests.remove();

        // Remember the largest startId, which is the most recent
        // request to start this service.
        if (maxStartId < request.getStartId()) {
          maxStartId = request.getStartId();
        }
      } else {
        // The service crashed, so restart it. Note that this leaves
        // the current request on the queue.
        bindToMarketBillingService();
        return;
      }
    }

    // If we get here then all the requests ran successfully. If maxStartId
    // is not -1, then one of the requests started the service, so we can
    // stop it now.
    if (maxStartId >= 0) {
      Log.i(TAG, "stopping service, startId: " + maxStartId);
      stopSelf(maxStartId);
    }
  }

  /**
   * This is called when we are connected to the MarketBillingService. This runs
   * in the main UI thread.
   */
  @Override
  public void onServiceConnected(final ComponentName name, final IBinder service) {
    Log.d(TAG, "Billing service connected");
    mService = IMarketBillingService.Stub.asInterface(service);
    runPendingRequests();
  }

  /**
   * This is called when we are disconnected from the MarketBillingService.
   */
  @Override
  public void onServiceDisconnected(final ComponentName name) {
    Log.w(TAG, "Billing service disconnected");
    mService = null;
  }

  /**
   * Unbinds from the MarketBillingService. Call this when the application
   * terminates to avoid leaking a ServiceConnection.
   */
  public void unbind() {
    try {
      unbindService(this);
    } catch (IllegalArgumentException e) {
      // This might happen if the service was disconnected
    }
  }

}
