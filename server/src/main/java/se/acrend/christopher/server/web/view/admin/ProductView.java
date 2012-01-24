package se.acrend.christopher.server.web.view.admin;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.server.persistence.DataConstants;
import se.acrend.christopher.server.service.impl.ConfigurationServiceImpl;
import se.acrend.christopher.server.util.ServiceLocator;
import se.acrend.christopher.server.web.util.EntityItem;

import com.google.appengine.api.datastore.Entity;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ProductView extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(ProductView.class);

  private IndexedContainer container;

  @Override
  public void attach() {
    super.attach();

    container = new IndexedContainer();
    container.addContainerProperty("productId", String.class, null);
    container.addContainerProperty("name", String.class, null);
    container.addContainerProperty("description", String.class, null);
    container.addContainerProperty("type", String.class, null);
    container.addContainerProperty("category", String.class, null);
    container.addContainerProperty("value", String.class, null);

    loadProducts();

    final EntityItem formItem = new EntityItem(new Entity(DataConstants.KIND_PRODUCT));
    formItem.addItemProperty("productId", String.class, "");
    formItem.addItemProperty("name", String.class, "");
    formItem.addItemProperty("description", String.class, "");
    formItem.addItemProperty("type", String.class, "");
    formItem.addItemProperty("category", String.class, "");
    formItem.addItemProperty("value", String.class, "");

    // Create the Form
    final Form form = new Form();
    form.setCaption("Ny produkt");
    form.setWriteThrough(false);
    form.setInvalidCommitted(false);

    // FieldFactory for customizing the fields and adding validators
    // personForm.setFormFieldFactory(new PersonFieldFactory());
    form.setItemDataSource(formItem);

    form.setVisibleItemProperties(Arrays.asList("productId", "name", "description", "type", "category", "value"));

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

          service.addProduct(formItem.getEntity());

          container.removeAllItems();
          formItem.setEntity(new Entity(DataConstants.KIND_PRODUCT));
          loadProducts();
        } catch (Exception e) {
          log.error("Kunde inte spara konfiguration", e);
        }
      }
    });
    buttons.addComponent(apply);
    form.getFooter().addComponent(buttons);
    form.getFooter().setMargin(false, false, true, true);

    Table table = new Table("Produkter", container);
    addComponent(table);
  }

  private void loadProducts() {
    ConfigurationServiceImpl service = ServiceLocator.getService(ConfigurationServiceImpl.class);
    List<Entity> products = service.getProducts();
    for (Entity p : products) {
      Item item = container.addItem(p.getKey());
      for (String id : p.getProperties().keySet()) {
        item.getItemProperty(id).setValue(p.getProperty(id));
      }
    }
  }
}
