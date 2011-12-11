package se.acrend.christopher.android.model;

import java.util.Calendar;

import se.acrend.christopher.android.util.ValidatorHelper;

public class MessageWrapper {

  private static final String TAG = "MessageWrapper";

  public static enum TicketType {
    Confirmation("Christopher.Bokningsbekräftelse"), SmsTicket("Christopher.Mobilbiljett");

    private String displayName;

    private TicketType(final String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }

  }

  private String from;
  private String to;
  private Calendar departure;
  private Calendar arrival;
  private String car;
  private String seat;
  private String code;
  private String train;
  private String message;
  private final TicketType ticketType;

  public MessageWrapper(final TicketType ticketType) {
    this.ticketType = ticketType;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(final String to) {
    this.to = to;
  }

  public Calendar getDeparture() {
    return departure;
  }

  public void setDeparture(final Calendar departure) {
    this.departure = departure;
  }

  public Calendar getArrival() {
    return arrival;
  }

  public void setArrival(final Calendar arrival) {
    this.arrival = arrival;
  }

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public String getCar() {
    return car;
  }

  public void setCar(final String car) {
    this.car = car;
  }

  public String getSeat() {
    return seat;
  }

  public void setSeat(final String seat) {
    this.seat = seat;
  }

  public String getTrain() {
    return train;
  }

  public void setTrain(final String train) {
    this.train = train;
  }

  public void validate() {
    ValidatorHelper.notEmpty(from, "Från");
    ValidatorHelper.notEmpty(to, "Till");
    ValidatorHelper.notEmpty(code, "Internetkod");

    ValidatorHelper.notEmpty(departure, "Avgång");
    ValidatorHelper.notEmpty(arrival, "Ankomst");

    if (departure.after(arrival)) {
      throw new IllegalArgumentException("Avgång kan inte vara efter ankomst.");
    }
  }

  public TicketType getTicketType() {
    return ticketType;
  }

  @Override
  public String toString() {
    return "Biljettkod: " + code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}