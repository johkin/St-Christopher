package se.acrend.christopher.android.activity.setup;

import roboguice.inject.InjectView;
import se.acrend.christopher.R;
import se.acrend.christopher.android.activity.actionbar.ActionBarActivity;
import se.acrend.christopher.android.preference.AccountHelper;
import se.acrend.christopher.android.preference.AccountHelper.InitAccountCallback;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.inject.Inject;

public class ChooseAccount extends ActionBarActivity {

  @Inject
  private Context context;
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
