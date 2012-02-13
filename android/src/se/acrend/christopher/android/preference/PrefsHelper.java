package se.acrend.christopher.android.preference;

import se.acrend.christopher.android.util.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.inject.Inject;

public class PrefsHelper {

  @Inject
  private Context context;

  public static SharedPreferences getPrefs(final Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static void setShowAbout(final boolean show, final Context context) {
    SharedPreferences prefs = getPrefs(context);
    Editor editor = prefs.edit();
    editor.putBoolean("showAbout", show);
    editor.commit();
  }

  public static boolean isDeleteProcessedMessages(final Context context) {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getBoolean("deleteProcessedMessage", false);
  }

  public boolean isReplaceTicket() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getBoolean("replaceTicket", false);
  }

  public boolean isProcessIncommingMessages() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getBoolean("processIncomingMessages", false);
  }

  public void setProcessIncommingMessages(final boolean processIncomingMessages) {
    SharedPreferences prefs = getPrefs(context);
    Editor editor = prefs.edit();
    editor.putBoolean("processIncomingMessages", processIncomingMessages);
    editor.commit();
  }

  public int getReadAheadMinutes() {
    SharedPreferences preferences = getPrefs(context);
    String strMinutes = preferences.getString("readAheadMinutes", "30");
    return Integer.parseInt(strMinutes);
  }

  public static boolean isShowAbout(final Context context) {
    PackageManager manager = context.getPackageManager();
    try {
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      SharedPreferences prefs = getPrefs(context);

      int versionCode = info.versionCode;
      int storedVersion = prefs.getInt("versionCode", -1);
      if (storedVersion != versionCode) {
        Editor editor = prefs.edit();
        editor.putInt("versionCode", versionCode);
        editor.commit();
        return true;
      }
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Kunde inte hämta information för paket: " + context.getPackageName(), e);
    }
  }

  public String getAccountName() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getString("account", null);
  }

  public void setAccountName(final String name) {
    SharedPreferences prefs = getPrefs(context);
    Editor editor = prefs.edit();
    editor.putString("account", name);
    editor.commit();
  }

  public String getRegistrationId() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getString(Constants.REGISTRATION_KEY, null);
  }

  public String getNotificationSound() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getString(Constants.NOTIFICATION_SOUND, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
  }

  public boolean isNotificationVibration() {
    SharedPreferences prefs = getPrefs(context);
    return prefs.getBoolean(Constants.NOTIFICATION_VIBRATION, true);
  }

  public void setRegistrationId(final String registrationId) {
    SharedPreferences prefs = getPrefs(context);
    Editor editor = prefs.edit();
    editor.putString(Constants.REGISTRATION_KEY, registrationId);
    editor.commit();
  }
}
