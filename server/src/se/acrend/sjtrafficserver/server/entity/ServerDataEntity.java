package se.acrend.sjtrafficserver.server.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class ServerDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
