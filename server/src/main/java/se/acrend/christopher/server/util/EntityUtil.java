package se.acrend.christopher.server.util;

import com.google.appengine.api.datastore.Entity;

public class EntityUtil {

  public static int getInt(final Entity entity, final String propertyName, final int defaultValue) {
    if (!entity.hasProperty(propertyName)) {
      return defaultValue;
    }
    Object property = entity.getProperty(propertyName);
    if (property instanceof Integer) {
      return ((Integer) property).intValue();
    }
    if (property instanceof Long) {
      return ((Long) property).intValue();
    }
    return defaultValue;
  }

}
