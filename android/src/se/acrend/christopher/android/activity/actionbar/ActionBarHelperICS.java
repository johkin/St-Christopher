package se.acrend.christopher.android.activity.actionbar;

import android.app.Activity;
import android.content.Context;

/**
 * An extension of {@link se.acrend.christopher.android.activity.actionbar.example.android.actionbarcompat.ActionBarHelper}
 * that provides Android 4.0-specific functionality for IceCreamSandwich
 * devices. It thus requires API level 14.
 */
public class ActionBarHelperICS extends ActionBarHelperHoneycomb {
  protected ActionBarHelperICS(final Activity activity) {
    super(activity);
  }

  @Override
  protected Context getActionBarThemedContext() {
    return mActivity.getActionBar().getThemedContext();
  }
}
