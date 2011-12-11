package se.acrend.christopher.shared.model;


public class PrepareBillingInfo extends AbstractResponse {

  private String nonce;
  private String marketLicenseKey;

  public String getNonce() {
    return nonce;
  }

  public void setNonce(final String nonce) {
    this.nonce = nonce;
  }

  public String getMarketLicenseKey() {
    return marketLicenseKey;
  }

  public void setMarketLicenseKey(final String marketLicenseKey) {
    this.marketLicenseKey = marketLicenseKey;
  }

}
