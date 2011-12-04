package se.acrend.christopher.server.entity;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;

public class ServerDataEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  private Key key;

  private String authString;

  private String marketLicenseKey;

  public Key getKey() {
    return key;
  }

  public void setKey(final Key key) {
    this.key = key;
  }

  public String getAuthString() {
    return authString;
  }

  public void setAuthString(final String authString) {
    this.authString = authString;
  }

  public String getMarketLicenseKey() {
    return marketLicenseKey;
  }

  public void setMarketLicenseKey(final String marketLicenseKey) {
    this.marketLicenseKey = marketLicenseKey;
  }

}
