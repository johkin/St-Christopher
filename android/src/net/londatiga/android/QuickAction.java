package net.londatiga.android;

import java.util.ArrayList;
import java.util.List;

import se.acrend.christopher.R;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

/**
 * QuickAction dialog.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net> Contributors: - Kevin Peck
 *         <kevinwpeck@gmail.com>
 */
public class QuickAction extends PopupWindows implements OnDismissListener {
  private ImageView mArrowUp;
  private ImageView mArrowDown;
  private Animation mTrackAnim;
  private LayoutInflater inflater;
  private ViewGroup mTrack;
  private OnActionItemClickListener mItemClickListener;
  private OnDismissListener mDismissListener;

  private final List<ActionItem> mActionItemList = new ArrayList<ActionItem>();

  private boolean mDidAction;
  private boolean mAnimateTrack;

  private int mChildPos;
  private int mAnimStyle;

  public static final int ANIM_GROW_FROM_LEFT = 1;
  public static final int ANIM_GROW_FROM_RIGHT = 2;
  public static final int ANIM_GROW_FROM_CENTER = 3;
  public static final int ANIM_AUTO = 4;

  /**
   * Constructor.
   * 
   * @param context
   *          Context
   */
  public QuickAction(final Context context) {
    super(context);

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    mTrackAnim = AnimationUtils.loadAnimation(context, R.anim.rail);

    mTrackAnim.setInterpolator(new Interpolator() {
      @Override
      public float getInterpolation(final float t) {
        // Pushes past the target area, then snaps back into place.
        // Equation for graphing: 1.2-((x*1.6)-1.1)^2
        final float inner = (t * 1.55f) - 1.1f;

        return 1.2f - (inner * inner);
      }
    });

    setRootViewId(R.layout.quickaction);

    mAnimStyle = ANIM_AUTO;
    mAnimateTrack = true;
    mChildPos = 0;
  }

  /**
   * Get action item at an index
   * 
   * @param index
   *          Index of item (position from callback)
   * @return Action Item at the position
   */
  public ActionItem getActionItem(final int index) {
    return mActionItemList.get(index);
  }

  /**
   * Set root view.
   * 
   * @param id
   *          Layout resource id
   */
  public void setRootViewId(final int id) {
    mRootView = inflater.inflate(id, null);
    mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);

    mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
    mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

    // This was previously defined on show() method, moved here to prevent force
    // close that occured
    // when tapping fastly on a view to show quickaction dialog.
    // Thanx to zammbi (github.com/zammbi)
    mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    setContentView(mRootView);
  }

  /**
   * Animate track.
   * 
   * @param mAnimateTrack
   *          flag to animate track
   */
  public void mAnimateTrack(final boolean mAnimateTrack) {
    this.mAnimateTrack = mAnimateTrack;
  }

  /**
   * Set animation style.
   * 
   * @param mAnimStyle
   *          animation style, default is set to ANIM_AUTO
   */
  public void setAnimStyle(final int mAnimStyle) {
    this.mAnimStyle = mAnimStyle;
  }

  /**
   * Add action item
   * 
   * @param action
   *          {@link ActionItem}
   */
  public void addActionItem(final ActionItem action) {
    mActionItemList.add(action);

    String title = action.getTitle();
    String text = action.getText();
    Drawable icon = action.getIcon();

    View container = inflater.inflate(R.layout.quickaction_item, null);

    ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
    TextView titleView = (TextView) container.findViewById(R.id.tv_title);
    TextView textView = (TextView) container.findViewById(R.id.tv_text);

    if (icon != null) {
      img.setImageDrawable(icon);
    } else {
      img.setVisibility(View.GONE);
    }

    if (title != null) {
      titleView.setText(title);
    } else {
      titleView.setVisibility(View.GONE);
    }
    if (text != null) {
      textView.setText(text);
    } else {
      textView.setVisibility(View.GONE);
    }

    final int pos = mChildPos;
    final int actionId = action.getActionId();

    container.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (mItemClickListener != null) {
          mItemClickListener.onItemClick(QuickAction.this, pos, actionId);
        }

        if (!getActionItem(pos).isSticky()) {
          mDidAction = true;

          // workaround for transparent background bug
          // thx to Roman Wozniak <roman.wozniak@gmail.com>
          v.post(new Runnable() {
            @Override
            public void run() {
              dismiss();
            }
          });
        }
      }
    });

    container.setFocusable(true);
    container.setClickable(true);

    mTrack.addView(container, mChildPos + 1);

    mChildPos++;
  }

  public void setOnActionItemClickListener(final OnActionItemClickListener listener) {
    mItemClickListener = listener;
  }

  /**
   * Show popup mWindow
   */
  public void show(final View anchor) {
    preShow();

    int[] location = new int[2];

    mDidAction = false;

    anchor.getLocationOnScreen(location);

    Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1]
        + anchor.getHeight());

    // mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
    // LayoutParams.WRAP_CONTENT));
    mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    int rootWidth = mRootView.getMeasuredWidth();
    int rootHeight = mRootView.getMeasuredHeight();

    int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
    // int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

    int xPos = (screenWidth - rootWidth) / 2;
    int yPos = anchorRect.top - rootHeight;

    boolean onTop = true;

    // display on bottom
    if (rootHeight > anchor.getTop()) {
      yPos = anchorRect.bottom;
      onTop = false;
    }

    showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX());

    setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

    mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

    if (mAnimateTrack) {
      mTrack.startAnimation(mTrackAnim);
    }
  }

  /**
   * Set animation style
   * 
   * @param screenWidth
   *          Screen width
   * @param requestedX
   *          distance from left screen
   * @param onTop
   *          flag to indicate where the popup should be displayed. Set TRUE if
   *          displayed on top of anchor and vice versa
   */
  private void setAnimationStyle(final int screenWidth, final int requestedX, final boolean onTop) {
    int arrowPos = requestedX - (mArrowUp.getMeasuredWidth() / 2);

    switch (mAnimStyle) {
    case ANIM_GROW_FROM_LEFT:
      mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
      break;

    case ANIM_GROW_FROM_RIGHT:
      mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
      break;

    case ANIM_GROW_FROM_CENTER:
      mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
      break;

    case ANIM_AUTO:
      if (arrowPos <= (screenWidth / 4)) {
        mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
      } else if ((arrowPos > (screenWidth / 4)) && (arrowPos < (3 * (screenWidth / 4)))) {
        mWindow
            .setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
      } else {
        mWindow
            .setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right : R.style.Animations_PopDownMenu_Right);
      }

      break;
    }
  }

  /**
   * Show arrow
   * 
   * @param whichArrow
   *          arrow type resource id
   * @param requestedX
   *          distance from left screen
   */
  private void showArrow(final int whichArrow, final int requestedX) {
    final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
    final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

    final int arrowWidth = mArrowUp.getMeasuredWidth();

    showArrow.setVisibility(View.VISIBLE);

    ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

    param.leftMargin = requestedX - (arrowWidth / 2);

    hideArrow.setVisibility(View.INVISIBLE);
  }

  /**
   * Set listener for window dismissed. This listener will only be fired if the
   * quicakction dialog is dismissed by clicking outside the dialog or clicking
   * on sticky item.
   */
  public void setOnDismissListener(final QuickAction.OnDismissListener listener) {
    setOnDismissListener(this);

    mDismissListener = listener;
  }

  @Override
  public void onDismiss() {
    if (!mDidAction && (mDismissListener != null)) {
      mDismissListener.onDismiss();
    }
  }

  /**
   * Listener for item click
   */
  public interface OnActionItemClickListener {
    public abstract void onItemClick(QuickAction source, int pos, int actionId);
  }

  /**
   * Listener for window dismiss
   */
  public interface OnDismissListener {
    public abstract void onDismiss();
  }
}