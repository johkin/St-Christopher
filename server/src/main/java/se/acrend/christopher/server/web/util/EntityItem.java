package se.acrend.christopher.server.web.util;

import java.lang.reflect.Constructor;
import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.PropertysetItem;

public class EntityItem extends PropertysetItem {

  private static final long serialVersionUID = 1L;

  private Entity entity;

  public EntityItem(final Entity entity) {
    setEntity(entity);
  }

  public void setEntity(final Entity entity) {
    this.entity = entity;

    for (String id : entity.getProperties().keySet()) {
      Object value = entity.getProperty(id);
      Property p = null;
      if (value instanceof Date) {
        p = new EntityProperty(this, id, Date.class);
      } else if (value instanceof String) {
        p = new EntityProperty(this, id, String.class);
      } else if (value instanceof Long) {
        p = new EntityProperty(this, id, Long.class);
      }
      addItemProperty(id, p);
    }
  }

  public void addItemProperty(final String id, final Class<?> cls, final Object value) {
    addItemProperty(id, new EntityProperty(this, id, cls));
    if (!entity.hasProperty(id)) {
      entity.setProperty(id, value);
    }
  }

  public Entity getEntity() {
    return entity;
  }

  public static class EntityProperty extends AbstractProperty {

    private final EntityItem item;
    private final String id;
    private final Class<?> type;

    public EntityProperty(final EntityItem item, final String id, final Class<?> type) {
      this.item = item;
      this.id = id;
      this.type = type;
    }

    @Override
    public Object getValue() {
      return item.getEntity().getProperty(id);
    }

    @Override
    public void setValue(final Object newValue) throws ReadOnlyException, ConversionException {
      // Checks the mode
      if (isReadOnly()) {
        throw new Property.ReadOnlyException();
      }

      // Tries to assign the compatible value directly
      if ((newValue == null) || type.isAssignableFrom(newValue.getClass())) {
        item.getEntity().setProperty(id, newValue);
      } else {
        try {
          // Gets the string constructor
          Constructor constr = getType().getConstructor(
              new Class[] { String.class });

          // Creates new object from the string
          Object value = constr
              .newInstance(new Object[] { newValue.toString() });

          item.getEntity().setProperty(id, newValue);
        } catch (final java.lang.Exception e) {
          throw new Property.ConversionException(e);
        }
      }

      fireValueChange();
    }

    @Override
    public Class<?> getType() {
      return type;
    }
  }

}
