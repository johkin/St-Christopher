package se.acrend.christopher.server.web.view.admin;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.server.service.impl.ConfigurationServiceImpl;
import se.acrend.christopher.server.util.ServiceLocator;
import se.acrend.christopher.server.web.util.EntityItem;

import com.google.appengine.api.datastore.Entity;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class ConfigurationView extends VerticalLayout {

  private static Logger log = LoggerFactory.getLogger(ConfigurationView.class);

  @Override
  public void attach() {
    super.attach();

    ConfigurationServiceImpl service = ServiceLocator.getService(ConfigurationServiceImpl.class);

    Entity data = service.getConfiguration();

    final EntityItem formItem = new EntityItem(data);
    formItem.addItemProperty("authString", String.class, "");
    formItem.addItemProperty("marketLicenseKey", String.class, "");

    // Create the Form
    final Form form = new Form();
    form.setCaption("Konfiguration");
    form.setWriteThrough(false);
    form.setInvalidCommitted(false);

    // FieldFactory for customizing the fields and adding validators
    // personForm.setFormFieldFactory(new PersonFieldFactory());
    form.setItemDataSource(formItem);

    form.setVisibleItemProperties(Arrays.asList("authString", "marketLicenseKey"));

    addComponent(form);

    // The cancel / apply buttons
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.setSpacing(true);
    Button discardChanges = new Button("Avbryt", new Button.ClickListener() {

      @Override
      public void buttonClick(final ClickEvent event) {
        form.discard();
      }
    });
    buttons.addComponent(discardChanges);
    buttons.setComponentAlignment(discardChanges, Alignment.MIDDLE_LEFT);

    Button apply = new Button("Spara", new Button.ClickListener() {
      @Override
      public void buttonClick(final ClickEvent event) {
        try {
          form.commit();
          ConfigurationServiceImpl service = ServiceLocator.getService(ConfigurationServiceImpl.class);

          service.updateConfiguration(formItem.getEntity());
        } catch (Exception e) {
          log.error("Kunde inte spara konfiguration", e);
        }
      }
    });
    buttons.addComponent(apply);
    form.getFooter().addComponent(buttons);
    form.getFooter().setMargin(false, false, true, true);

  }
}
