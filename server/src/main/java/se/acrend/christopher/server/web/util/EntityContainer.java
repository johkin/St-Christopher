package se.acrend.christopher.server.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.acrend.christopher.server.web.util.EntityItem.EntityProperty;

import com.google.appengine.api.datastore.Key;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;

public class EntityContainer extends AbstractInMemoryContainer<Key, String, EntityItem> {

  private final Map<String, EntityProperty> properties = new HashMap<String, EntityItem.EntityProperty>();

  private final List<EntityItem> items = new ArrayList<EntityItem>();

  public EntityContainer() {
    super();
  }

  @Override
  public Collection<?> getContainerPropertyIds() {
    return properties.keySet();
  }

  @Override
  public Property getContainerProperty(final Object itemId, final Object propertyId) {
    return properties.get(propertyId);
  }

  @Override
  public Class<?> getType(final Object propertyId) {
    return properties.get(propertyId).getType();
  }

  @Override
  protected EntityItem getUnfilteredItem(final Object itemId) {
    // TODO Auto-generated method stub
    return null;
  }

}
