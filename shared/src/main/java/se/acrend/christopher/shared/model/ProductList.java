package se.acrend.christopher.shared.model;

import java.util.ArrayList;
import java.util.List;

public class ProductList extends AbstractResponse {

  private final List<Product> products = new ArrayList<ProductList.Product>();

  public List<Product> getProducts() {
    return products;
  }

  public void addProduct(final Product p) {
    products.add(p);
  }

  public static class Product {

    private String productId;
    private String name;
    private String description;

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

    public void clear() {
      productId = null;
      name = null;
      description = null;
    }

    public Product copy() {
      Product p = new Product();
      p.productId = productId;
      p.name = name;
      p.description = description;

      return p;
    }
  }
}
