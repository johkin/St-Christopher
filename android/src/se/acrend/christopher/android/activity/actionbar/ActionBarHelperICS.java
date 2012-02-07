package se.acrend.christopher.android.activity.actionbar;

import se.acrend.christopher.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/**
 * An extension of
 * {@link se.acrend.christopher.android.activity.actionbar.example.android.actionbarcompat.ActionBarHelper}
 * that provides Android 4.0-specific functionality for IceCreamSandwich
 * devices. It thus requires API level 14.
 */
public class ActionBarHelperICS extends ActionBarHelperHoneycomb {
  protected ActionBarHelperICS(final Activity activity) {
    super(activity);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity.getActionBar().setIcon(R.drawable.ic_logo);
  }

  @Override
  protected Context getActionBarThemedContext() {
    return mActivity.getActionBar().getThemedContext();
  }

  @Override
  public void setHomeButtonEnabled(final boolean enabled) {
    super.setHomeButtonEnabled(enabled);
    mActivity.getActionBar().setHomeButtonEnabled(enabled);
    mActivity.getActionBar().setDisplayShowHomeEnabled(enabled);
  }
}
