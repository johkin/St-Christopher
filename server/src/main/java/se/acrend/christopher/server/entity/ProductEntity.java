package se.acrend.christopher.server.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class ProductEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum ProductType {
    Day, Count
  }

  public enum ProductCategory {
    Notification, TravelWarrant
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  private String productId;
  private String name;
  private String description;
  private String type;
  private String category;
  private String value;

  public Key getKey() {
    return key;
  }

  public void setKey(final Key key) {
    this.key = key;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(final String productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(final String category) {
    this.category = category;
  }
}
