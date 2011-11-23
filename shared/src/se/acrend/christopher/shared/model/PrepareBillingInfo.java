package se.acrend.christopher.shared.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "prepareBillingInfo")
public class PrepareBillingInfo extends AbstractResponse {

  private String nonce;
  private String marketLicenseKey;

  @XmlElement(name = "nonce")
  public String getNonce() {
    return nonce;
  }

  public void setNonce(final String nonce) {
    this.nonce = nonce;
  }

  @XmlElement(name = "marketLicenseKey")
  public String getMarketLicenseKey() {
    return marketLicenseKey;
  }

  public void setMarketLicenseKey(final String marketLicenseKey) {
    this.marketLicenseKey = marketLicenseKey;
  }

}
