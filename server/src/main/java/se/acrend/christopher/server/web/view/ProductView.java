package se.acrend.christopher.server.web.view;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.acrend.christopher.server.entity.ProductEntity;
import se.acrend.christopher.server.service.impl.ConfigurationServiceImpl;
import se.acrend.christopher.server.util.ServiceLocator;

import com.google.appengine.api.datastore.Key;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ProductView extends VerticalLayout {

  private static Logger log = LoggerFactory.getLogger(ProductView.class);

  private BeanContainer<Key, ProductEntity> container;

  @Override
  public void attach() {
    super.attach();

    container = new BeanContainer<Key, ProductEntity>(ProductEntity.class);
    // container.addContainerProperty("productId", String.class, null);
    // container.addContainerProperty("name", String.class, null);
    // container.addContainerProperty("description", String.class, null);
    // container.addContainerProperty("type", String.class, null);
    // container.addContainerProperty("category", String.class, null);
    // container.addContainerProperty("value", String.class, null);
    container.setBeanIdProperty("key");

    loadProducts();

    // for (ProductEntity product : products) {
    // container.addItem(product.getKey(), product);
    // }

    final BeanItem<ProductEntity> formItem = new BeanItem<ProductEntity>(new ProductEntity());

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

          service.addProduct(formItem.getBean());
          container.removeAllItems();
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
    List<ProductEntity> products = service.getProducts();
    for (ProductEntity p : products) {
      container.addBean(p);
    }
  }
}
