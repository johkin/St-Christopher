package se.acrend.christopher.shared.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "productList")
public class ProductList extends AbstractResponse {

  private final List<Product> products = new ArrayList<ProductList.Product>();

  @XmlElement(name = "product")
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

    @XmlElement(name = "productId")
    public String getProductId() {
      return productId;
    }

    public void setProductId(final String productId) {
      this.productId = productId;
    }

    @XmlElement(name = "name")
    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    @XmlElement(name = "description")
    public String getDescription() {
      return description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }
  }
}
