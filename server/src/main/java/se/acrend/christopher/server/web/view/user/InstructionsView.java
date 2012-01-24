package se.acrend.christopher.server.web.view.user;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.VerticalLayout;

public class InstructionsView extends VerticalLayout {

  private static Logger log = LoggerFactory.getLogger(InstructionsView.class);

  @Override
  public void attach() {
    super.attach();
    String filename = "/help.html";
    // ClassResource resource = new ClassResource(this.getClass(), filename,
    // getApplication());
    InputStream stream = this.getClass().getResourceAsStream(filename);
    try {
      CustomLayout layout = new CustomLayout(stream);
      addComponent(layout);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // log.debug("Stream: {}", stream);
    // log.debug("Mimetype: {}", resource.getMIMEType());
    // log.debug("Stream: {}", resource.getStream().getStream());
    //
    // Embedded embedded = new Embedded("Instruktioner", resource);
    // embedded.setType(Embedded.TYPE_OBJECT);
    // embedded.setSizeFull();
    // addComponent(embedded);
  }
}
