package se.acrend.christopher.android.application;

import java.util.List;

import roboguice.application.RoboApplication;
import se.acrend.christopher.android.module.Sj2CalModule;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.inject.Module;

public class ChristopherApp extends RoboApplication {

  private static final String COM_JB_GOSMS = "com.jb.gosms";
  private static final String SJ_2_CAL = "se.acrend.sj2cal";

  private static final String TAG = "Sj2CalApp";

  @Override
  protected void addApplicationModules(final List<Module> modules) {
    modules.add(new Sj2CalModule());
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO Lägg kontroll av OS-version för att avgöra vad som ska vara aktivt,
    // ställ in default-värden.
  }

  public boolean isGoSmsInstalled() {
    PackageManager packageManager = getPackageManager();
    try {
      packageManager.getApplicationInfo(COM_JB_GOSMS, PackageManager.GET_META_DATA);
      return true;
    } catch (NameNotFoundException e) {
      Log.d(TAG, "GoSMS not installed.");
      return false;
    }
  }
}
