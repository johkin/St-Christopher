package se.acrend.christopher.android.preference;

import se.acrend.christopher.android.activity.SubscriptionDetails;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class OpenSubscriptionPreference extends Preference {

  public OpenSubscriptionPreference(final Context context) {
    super(context);
  }

  public OpenSubscriptionPreference(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public OpenSubscriptionPreference(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    getContext().startActivity(new Intent(this.getContext(), SubscriptionDetails.class));
  }
}
