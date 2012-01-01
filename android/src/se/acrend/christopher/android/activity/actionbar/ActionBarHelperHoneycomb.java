package se.acrend.christopher.android.activity.actionbar;

import se.acrend.christopher.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * An extension of {@link ActionBarHelper} that provides Android 3.0-specific
 * functionality for Honeycomb tablets. It thus requires API level 11.
 */
public class ActionBarHelperHoneycomb extends ActionBarHelper {
  private Menu mOptionsMenu;
  private View mRefreshIndeterminateProgressView = null;

  protected ActionBarHelperHoneycomb(final Activity activity) {
    super(activity);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    mOptionsMenu = menu;
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void setRefreshActionItemState(final boolean refreshing) {
    // On Honeycomb, we can set the state of the refresh button by giving it a
    // custom
    // action view.
    if (mOptionsMenu == null) {
      return;
    }

    final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
    if (refreshItem != null) {
      if (refreshing) {
        if (mRefreshIndeterminateProgressView == null) {
          LayoutInflater inflater = (LayoutInflater)
              getActionBarThemedContext().getSystemService(
                  Context.LAYOUT_INFLATER_SERVICE);
          mRefreshIndeterminateProgressView = inflater.inflate(
              R.layout.actionbar_indeterminate_progress, null);
        }

        refreshItem.setActionView(mRefreshIndeterminateProgressView);
      } else {
        refreshItem.setActionView(null);
      }
    }
  }

  /**
   * Returns a {@link Context} suitable for inflating layouts for the action
   * bar. The implementation for this method in {@link ActionBarHelperICS} asks
   * the action bar for a themed context.
   */
  protected Context getActionBarThemedContext() {
    return mActivity;
  }
}
