package se.acrend.christophertestclient.application;

import java.util.List;

import roboguice.application.RoboApplication;

import com.google.inject.Module;

public class ChristopherApp extends RoboApplication {

  @Override
  protected void addApplicationModules(final List<Module> modules) {
  }

  @Override
  public void onCreate() {
    super.onCreate();

  }

  @Override
  public void onTerminate() {
  }
}
