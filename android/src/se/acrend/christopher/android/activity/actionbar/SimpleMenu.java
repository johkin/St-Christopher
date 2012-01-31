package se.acrend.christopher.android.activity.actionbar;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

/**
 * A <em>really</em> dumb implementation of the {@link android.view.Menu}
 * interface, that's only useful for our actionbar-compat purposes. See
 * <code>com.android.internal.view.menu.MenuBuilder</code> in AOSP for a more
 * complete implementation.
 */
public class SimpleMenu implements Menu {

  private final Context mContext;
  private final Resources mResources;

  private final ArrayList<SimpleMenuItem> mItems;

  public SimpleMenu(final Context context) {
    mContext = context;
    mResources = context.getResources();
    mItems = new ArrayList<SimpleMenuItem>();
  }

  public Context getContext() {
    return mContext;
  }

  public Resources getResources() {
    return mResources;
  }

  @Override
  public MenuItem add(final CharSequence title) {
    return addInternal(0, 0, title);
  }

  @Override
  public MenuItem add(final int titleRes) {
    return addInternal(0, 0, mResources.getString(titleRes));
  }

  @Override
  public MenuItem add(final int groupId, final int itemId, final int order, final CharSequence title) {
    return addInternal(itemId, order, title);
  }

  @Override
  public MenuItem add(final int groupId, final int itemId, final int order, final int titleRes) {
    return addInternal(itemId, order, mResources.getString(titleRes));
  }

  /**
   * Adds an item to the menu. The other add methods funnel to this.
   */
  private MenuItem addInternal(final int itemId, final int order, final CharSequence title) {
    final SimpleMenuItem item = new SimpleMenuItem(this, itemId, order, title);
    mItems.add(findInsertIndex(mItems, order), item);
    return item;
  }

  private static int findInsertIndex(final ArrayList<? extends MenuItem> items, final int order) {
    for (int i = items.size() - 1; i >= 0; i--) {
      MenuItem item = items.get(i);
      if (item.getOrder() <= order) {
        return i + 1;
      }
    }

    return 0;
  }

  public int findItemIndex(final int id) {
    final int size = size();

    for (int i = 0; i < size; i++) {
      SimpleMenuItem item = mItems.get(i);
      if (item.getItemId() == id) {
        return i;
      }
    }

    return -1;
  }

  @Override
  public void removeItem(final int itemId) {
    removeItemAtInt(findItemIndex(itemId));
  }

  private void removeItemAtInt(final int index) {
    if ((index < 0) || (index >= mItems.size())) {
      return;
    }
    mItems.remove(index);
  }

  @Override
  public void clear() {
    mItems.clear();
  }

  @Override
  public MenuItem findItem(final int id) {
    final int size = size();
    for (int i = 0; i < size; i++) {
      SimpleMenuItem item = mItems.get(i);
      if (item.getItemId() == id) {
        return item;
      }
    }

    return null;
  }

  @Override
  public int size() {
    return mItems.size();
  }

  @Override
  public MenuItem getItem(final int index) {
    return mItems.get(index);
  }

  // Unsupported operations.

  @Override
  public SubMenu addSubMenu(final CharSequence charSequence) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public SubMenu addSubMenu(final int titleRes) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public SubMenu addSubMenu(final int groupId, final int itemId, final int order, final CharSequence title) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public SubMenu addSubMenu(final int groupId, final int itemId, final int order, final int titleRes) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public int addIntentOptions(final int i, final int i1, final int i2, final ComponentName componentName,
      final Intent[] intents, final Intent intent, final int i3, final MenuItem[] menuItems) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void removeGroup(final int i) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void setGroupCheckable(final int i, final boolean b, final boolean b1) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void setGroupVisible(final int i, final boolean b) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void setGroupEnabled(final int i, final boolean b) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public boolean hasVisibleItems() {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public boolean performShortcut(final int i, final KeyEvent keyEvent, final int i1) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public boolean isShortcutKey(final int i, final KeyEvent keyEvent) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public boolean performIdentifierAction(final int i, final int i1) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }

  @Override
  public void setQwertyMode(final boolean b) {
    throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
  }
}
