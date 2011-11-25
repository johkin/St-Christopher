package se.acrend.christopher.android.parser.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import se.acrend.christopher.shared.exception.PermanentException;
import se.acrend.christopher.shared.exception.TemporaryException;
import se.acrend.christopher.shared.model.ProductList;
import se.acrend.christopher.shared.model.ProductList.Product;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

public class ProductParser extends AbstactResponseParser {

  private static final String TAG = "ProductParser";

  public ProductList parse(final InputStream inputStream) {
    final ProductList model = new ProductList();
    final Product product = new Product();

    RootElement root = new RootElement("productList");
    handleResponse(root, model);
    Element productElement = root.getChild("product");
    productElement.setElementListener(new ElementListener() {

      @Override
      public void start(final Attributes attributes) {
        product.clear();
      }

      @Override
      public void end() {
        model.addProduct(product.copy());

      }
    });
    productElement.getChild("productId").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        product.setProductId(body);
      }
    });
    productElement.getChild("name").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        product.setName(body);
      }
    });
    productElement.getChild("description").setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(final String body) {
        product.setDescription(body);
      }
    });

    try {
      Xml.parse(inputStream, Xml.Encoding.UTF_8, root.getContentHandler());
    } catch (SAXException e) {
      Log.e(TAG, "Could not parse content.", e);
      throw new PermanentException("Could not parse content.", e);
    } catch (IOException e) {
      Log.e(TAG, "Could not read content.", e);
      throw new TemporaryException("Could not read content.", e);
    }

    return model;
  }
}
