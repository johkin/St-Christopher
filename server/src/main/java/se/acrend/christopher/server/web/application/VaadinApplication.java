package se.acrend.christopher.server.web.application;

import org.springframework.stereotype.Component;

import se.acrend.christopher.server.web.view.MainView;

import com.vaadin.Application;

@Component
public class VaadinApplication extends Application {

  @Override
  public void init() {
    setMainWindow(new MainView());

  }
}
