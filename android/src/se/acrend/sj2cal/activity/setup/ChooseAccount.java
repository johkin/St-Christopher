package se.acrend.sj2cal.activity.setup;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import se.acrend.sj2cal.R;
import se.acrend.sj2cal.preference.AccountHelper;
import se.acrend.sj2cal.preference.AccountHelper.InitAccountCallback;
import se.acrend.sj2cal.preference.PrefsHelper;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.inject.Inject;

public class ChooseAccount extends RoboActivity {

  private static final String TAG = "ChooseAccount";

  private static final String ACCOUNT_TYPE = "com.google";

  private AccountManager accountManager;
  @Inject
  private Context context;
  @Inject
  private PrefsHelper prefsHelper;
  @Inject
  private AccountHelper accountHelper;

  @InjectView(R.id.choose_account_nextButton)
  private Button next;
  @InjectView(R.id.choose_account_account)
  private Spinner accountSpinner;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.choose_account);
    setTitle(R.string.prefs_category_account_title);

    accountManager = AccountManager.get(context);

    String[] accountNames = accountHelper.getAccountNames();
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
        accountNames);
    accountSpinner.setAdapter(adapter);

    next.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(final View v) {
        int position = accountSpinner.getSelectedItemPosition();
        final String name = adapter.getItem(position);

        accountHelper.initAccount(name, ChooseAccount.this, new InitAccountCallback() {

          @Override
          public void accountInitalized(final String accountName) {
            setResult(RESULT_OK);
            finish();
          }
        });
      }
    });
  }
}
