package se.acrend.christopher.android.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import se.acrend.christopher.android.model.MessageWrapper;
import se.acrend.christopher.android.model.MessageWrapper.TicketType;
import se.acrend.christopher.android.util.DateUtil;

public class ConfirmationParser extends MessageParserBase implements MessageParser {

  // * SJ * BKD3723G0002 Datum: 110114 Avg: Norrköping C 16.24
  // Ank: Stockholm C 17.39 Vagn: 2 Plats: 25
  // Internet ombord X2000/Dubbeldäckare Kod: BKD3723G0002

  private DateFormat format = null;

  public ConfirmationParser() {
    super();
    format = DateUtil.createDateFormat("yyMMddHH.mm");
  }

  @Override
  public boolean supports(final String message) {
    return message.contains("* SJ *");
  }

  @Override
  public MessageWrapper parse(final String message) {
    MessageWrapper ticket = new MessageWrapper(TicketType.Confirmation);

    ticket.setCode(findValue(message, "\\* SJ \\* (\\D{3}\\d{4}\\D\\d{4})", "Biljettkod"));
    String date = findValue(message, "Datum: (\\d{6})", "datum");
    String from = findValue(message, "Avg: (.+) \\d{1,2}\\.\\d{2} Ank", "avgångsort");
    ticket.setFrom(from);
    String depTime = findValue(message, "Avg: .+ (\\d{1,2}\\.\\d{2}) Ank", "avgångstid");
    String to = findValue(message, "Ank: (.+) \\d{1,2}\\.", "ankomstort");
    String arrTime = findValue(message, "Ank: .+ (\\d{1,2}\\.\\d{2})", "ankomsttid");
    String car = findValue(message, "Vagn: (\\d{1,2})", "vagn");
    String seat = findValue(message, "Plats: (\\d{1,3})", "plats");

    try {
      Calendar departure = Calendar.getInstance();
      departure.setTime(format.parse(date + depTime));
      ticket.setDeparture(departure);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Kunde inte tolka avgångsdatum.", e);
    }
    ticket.setTo(to);
    try {
      Calendar arrival = Calendar.getInstance();
      arrival.setTime(format.parse(date + arrTime));
      if (arrival.before(ticket.getDeparture())) {
        arrival.add(Calendar.DAY_OF_YEAR, 1);
      }
      ticket.setArrival(arrival);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Kunde inte tolka ankomstdatum.", e);
    }
    if ((car != null) && (car.length() > 0)) {
      ticket.setCar(car);
    }
    if ((seat != null) && (seat.length() > 0)) {
      ticket.setSeat(seat);
    }

    ticket.validate();

    return ticket;
  }
}
