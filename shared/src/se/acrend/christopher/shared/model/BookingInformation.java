package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import se.acrend.christopher.shared.util.DateTimeAdapter;

@XmlRootElement(name = "bookingInformation")
public class BookingInformation extends AbstractResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String departureTrack;
  private String arrivalTrack;

  private Date actualDeparture;
  private Date estimatedDeparture;

  private Date actualArrival;
  private Date estimatedArrival;

  private Date guessedArrival;
  private Date guessedDeparture;

  @XmlElement(name = "departureTrack")
  public String getDepartureTrack() {
    return departureTrack;
  }

  public void setDepartureTrack(final String departureTrack) {
    this.departureTrack = departureTrack;
  }

  @XmlElement(name = "arrivalTrack")
  public String getArrivalTrack() {
    return arrivalTrack;
  }

  public void setArrivalTrack(final String arrivalTrack) {
    this.arrivalTrack = arrivalTrack;
  }

  @XmlElement(name = "actualDeparture")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getActualDeparture() {
    return actualDeparture;
  }

  public void setActualDeparture(final Date actualDeparture) {
    this.actualDeparture = actualDeparture;
  }

  @XmlElement(name = "estimatedDeparture")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getEstimatedDeparture() {
    return estimatedDeparture;
  }

  public void setEstimatedDeparture(final Date estimatedDeparture) {
    this.estimatedDeparture = estimatedDeparture;
  }

  @XmlElement(name = "estimatedArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getEstimatedArrival() {
    return estimatedArrival;
  }

  public void setEstimatedArrival(final Date estimatedArrival) {
    this.estimatedArrival = estimatedArrival;
  }

  @XmlElement(name = "actualArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getActualArrival() {
    return actualArrival;
  }

  public void setActualArrival(final Date actualArrival) {
    this.actualArrival = actualArrival;
  }

  @XmlElement(name = "guessedArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getGuessedArrival() {
    return guessedArrival;
  }

  public void setGuessedArrival(final Date guessedArrival) {
    this.guessedArrival = guessedArrival;
  }

  @XmlElement(name = "guessedDeparture")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Date getGuessedDeparture() {
    return guessedDeparture;
  }

  public void setGuessedDeparture(final Date guessedDeparture) {
    this.guessedDeparture = guessedDeparture;
  }
}
