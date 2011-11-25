package se.acrend.christopher.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class DotView extends View {

  private final Paint paint;

  private boolean value = false;

  public DotView(final Context context) {
    this(context, null);
  }

  public DotView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DotView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    paint = new Paint();
    paint.setColor(Color.RED);
    paint.setStyle(Style.FILL);
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    if (value) {
      paint.setColor(Color.GREEN);
    } else {
      paint.setColor(Color.RED);
    }
    canvas.drawCircle(10, 10, 6, paint);
    super.onDraw(canvas);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    setMeasuredDimension(20, 20);
  }

  public void setValue(final boolean value) {
    this.value = value;
    invalidate();
  }

}
