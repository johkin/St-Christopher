package se.acrend.christopher.server.web.view;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.server.entity.ServerDataEntity;
import se.acrend.christopher.server.service.impl.ConfigurationServiceImpl;
import se.acrend.christopher.server.util.ContextUtil;

import com.vaadin.data.util.BeanItem;
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

    ConfigurationServiceImpl service = ContextUtil.getBean(ConfigurationServiceImpl.class);

    ServerDataEntity data = service.getConfiguration();

    final BeanItem<ServerDataEntity> formItem = new BeanItem<ServerDataEntity>(data);

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
          ConfigurationServiceImpl service = ContextUtil.getBean(ConfigurationServiceImpl.class);

          service.updateConfiguration(formItem.getBean());
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
