package se.acrend.christopher.android.activity.actionbar;

import roboguice.activity.RoboActivity;
import se.acrend.christopher.android.activity.TicketTabActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * A base activity that defers common functionality across app activities to an
 * {@link ActionBarHelper}. NOTE: dynamically marking menu items as
 * invisible/visible is not currently supported. NOTE: this may used with the
 * Android Compatibility Package by extending
 * android.support.v4.app.FragmentActivity instead of {@link Activity}.
 */
public abstract class ActionBarActivity extends RoboActivity {
  final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

  /**
   * Returns the {@link ActionBarHelper} for this activity.
   */
  protected ActionBarHelper getActionBarHelper() {
    return mActionBarHelper;
  }

  /** {@inheritDoc} */
  @Override
  public MenuInflater getMenuInflater() {
    return mActionBarHelper.getMenuInflater(super.getMenuInflater());
  }

  /** {@inheritDoc} */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActionBarHelper.onCreate(savedInstanceState);
  }

  /** {@inheritDoc} */
  @Override
  protected void onPostCreate(final Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mActionBarHelper.onPostCreate(savedInstanceState);
  }

  /**
   * Base action bar-aware implementation for
   * {@link Activity#onCreateOptionsMenu(android.view.Menu)}. Note: marking menu
   * items as invisible/visible is not currently supported.
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    boolean retValue = false;
    retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
    retValue |= super.onCreateOptionsMenu(menu);
    return retValue;
  }

  /** {@inheritDoc} */
  @Override
  protected void onTitleChanged(final CharSequence title, final int color) {
    mActionBarHelper.onTitleChanged(title, color);
    super.onTitleChanged(title, color);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      if (getActionBarHelper().isHomeButtonEnabled()) {
        startActivity(new Intent(this, TicketTabActivity.class));
        finish();
        return true;
      }
    default:
      return super.onOptionsItemSelected(item);
    }
  }

}
