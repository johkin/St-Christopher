package net.londatiga.android;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Action item, displayed as menu with icon and text.
 * 
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 * 
 *         Contributors: - Kevin Peck <kevinwpeck@gmail.com>
 * 
 */
public class ActionItem {
  private Drawable icon;
  private Bitmap thumb;
  private String title;
  private final String text;
  private int actionId = -1;
  private boolean selected;
  private boolean sticky;

  /**
   * Constructor
   * 
   * @param actionId
   *          Action id for case statements
   * @param title
   *          Title
   * @param icon
   *          Icon to use
   */
  public ActionItem(final int actionId, final String title, final Drawable icon, final String text) {
    this.title = title;
    this.icon = icon;
    this.actionId = actionId;
    this.text = text;
  }

  /**
   * Constructor
   */
  public ActionItem() {
    this(-1, null, null, null);
  }

  /**
   * Constructor
   * 
   * @param actionId
   *          Action id of the item
   * @param title
   *          Text to show for the item
   */
  public ActionItem(final int actionId, final String title) {
    this(actionId, title, null, null);
  }

  /**
   * Constructor
   * 
   * @param actionId
   *          Action id of the item
   * @param title
   *          Text to show for the item
   */
  public ActionItem(final int actionId, final String title, final String text) {
    this(actionId, title, null, text);
  }

  /**
   * Constructor
   * 
   * @param icon
   *          {@link Drawable} action icon
   */
  public ActionItem(final Drawable icon) {
    this(-1, null, icon, null);
  }

  /**
   * Constructor
   * 
   * @param actionId
   *          Action ID of item
   * @param icon
   *          {@link Drawable} action icon
   */
  public ActionItem(final int actionId, final Drawable icon) {
    this(actionId, null, icon, null);
  }

  /**
   * Set action title
   * 
   * @param title
   *          action title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Get action title
   * 
   * @return action title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Set action icon
   * 
   * @param icon
   *          {@link Drawable} action icon
   */
  public void setIcon(final Drawable icon) {
    this.icon = icon;
  }

  /**
   * Get action icon
   * 
   * @return {@link Drawable} action icon
   */
  public Drawable getIcon() {
    return this.icon;
  }

  /**
   * Set action id
   * 
   * @param actionId
   *          Action id for this action
   */
  public void setActionId(final int actionId) {
    this.actionId = actionId;
  }

  /**
   * @return Our action id
   */
  public int getActionId() {
    return actionId;
  }

  /**
   * Set sticky status of button
   * 
   * @param sticky
   *          true for sticky, pop up sends event but does not disappear
   */
  public void setSticky(final boolean sticky) {
    this.sticky = sticky;
  }

  /**
   * @return true if button is sticky, menu stays visible after press
   */
  public boolean isSticky() {
    return sticky;
  }

  /**
   * Set selected flag;
   * 
   * @param selected
   *          Flag to indicate the item is selected
   */
  public void setSelected(final boolean selected) {
    this.selected = selected;
  }

  /**
   * Check if item is selected
   * 
   * @return true or false
   */
  public boolean isSelected() {
    return this.selected;
  }

  /**
   * Set thumb
   * 
   * @param thumb
   *          Thumb image
   */
  public void setThumb(final Bitmap thumb) {
    this.thumb = thumb;
  }

  /**
   * Get thumb image
   * 
   * @return Thumb image
   */
  public Bitmap getThumb() {
    return this.thumb;
  }

  public String getText() {
    return text;
  }
}