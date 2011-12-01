package se.acrend.christopher.server.web.view;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MainView extends Window {

  private Panel content;

  @Override
  public void attach() {
    super.attach();

    setCaption("St Cristopher");

    VerticalLayout layout = new VerticalLayout();

    layout.addComponent(new Label("St Christopher"));

    MenuBar menuBar = new MenuBar();
    layout.addComponent(menuBar);

    menuBar.addItem("Mina bokningar", null);
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserAdmin()) {
      MenuItem adminMenu = menuBar.addItem("Administration", null, null);

      adminMenu.addItem("Konfiguration", new Command() {

        @Override
        public void menuSelected(final MenuItem selectedItem) {
          setContent(new ConfigurationView());
        }
      });
      adminMenu.addItem("Produkter", null);
      adminMenu.addItem("Alla bokningar", null);
    }

    content = new Panel();

    layout.addComponent(content);

    addComponent(layout);
  }

  public void setContent(final AbstractComponentContainer container) {
    content.removeAllComponents();
    content.addComponent(container);
  }

}
