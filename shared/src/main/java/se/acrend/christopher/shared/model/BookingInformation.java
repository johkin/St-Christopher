package se.acrend.christopher.shared.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import se.acrend.christopher.shared.util.DateTimeAdapter;

@XmlRootElement(name = "bookingInformation")
public class BookingInformation extends AbstractResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String departureTrack;
  private String arrivalTrack;

  private Calendar actualDeparture;
  private Calendar estimatedDeparture;

  private Calendar actualArrival;
  private Calendar estimatedArrival;

  private Calendar guessedArrival;
  private Calendar guessedDeparture;

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
  public Calendar getActualDeparture() {
    return actualDeparture;
  }

  public void setActualDeparture(final Calendar actualDeparture) {
    this.actualDeparture = actualDeparture;
  }

  @XmlElement(name = "estimatedDeparture")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getEstimatedDeparture() {
    return estimatedDeparture;
  }

  public void setEstimatedDeparture(final Calendar estimatedDeparture) {
    this.estimatedDeparture = estimatedDeparture;
  }

  @XmlElement(name = "estimatedArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getEstimatedArrival() {
    return estimatedArrival;
  }

  public void setEstimatedArrival(final Calendar estimatedArrival) {
    this.estimatedArrival = estimatedArrival;
  }

  @XmlElement(name = "actualArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getActualArrival() {
    return actualArrival;
  }

  public void setActualArrival(final Calendar actualArrival) {
    this.actualArrival = actualArrival;
  }

  @XmlElement(name = "guessedArrival")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getGuessedArrival() {
    return guessedArrival;
  }

  public void setGuessedArrival(final Calendar guessedArrival) {
    this.guessedArrival = guessedArrival;
  }

  @XmlElement(name = "guessedDeparture")
  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public Calendar getGuessedDeparture() {
    return guessedDeparture;
  }

  public void setGuessedDeparture(final Calendar guessedDeparture) {
    this.guessedDeparture = guessedDeparture;
  }
}
