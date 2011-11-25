package se.acrend.christopher.android.preference;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.inject.Inject;

public class AccountHelper {

  private static final String TAG = "AccountHelper";

  private static final String ACCOUNT_TYPE = "com.google";

  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;

  public String[] getAccountNames() {
    AccountManager accountManager = AccountManager.get(context);

    final Account[] accountArr = accountManager.getAccountsByType(ACCOUNT_TYPE);
    String[] accountNames = new String[accountArr.length];
    for (int i = 0; i < accountArr.length; i++) {
      accountNames[i] = accountArr[i].name;
    }
    return accountNames;
  }

  public void initAccount(final String name, final Activity activity, final InitAccountCallback callback) {
    AccountManager accountManager = AccountManager.get(context);

    for (Account a : accountManager.getAccountsByType(ACCOUNT_TYPE)) {
      if (a.name.equals(name)) {
        accountManager.getAuthToken(a, "ah", null, activity, new AccountManagerCallback<Bundle>() {

          @Override
          public void run(final AccountManagerFuture<Bundle> result) {
            try {
              // Anropa getResult för att få eventuella felmeddelanden
              result.getResult();
              Log.e(TAG, "Token genererad för konto: " + name);
              prefsHelper.setAccountName(name);
              if (callback != null) {
                callback.accountInitalized(name);
              }
            } catch (OperationCanceledException e) {
              Log.e(TAG, "Användaren avbröt inloggning.", e);
            } catch (AuthenticatorException e) {
              Log.e(TAG, "Fel vid token-generering.", e);
            } catch (IOException e) {
              Log.e(TAG, "IO-fel vid token-generering.", e);
            }
          }
        }, null);
      }
    }
  }

  public static interface InitAccountCallback {

    void accountInitalized(String accountName);
  }
}
