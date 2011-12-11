package se.acrend.christopher.android.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import se.acrend.christopher.android.model.MessageWrapper;
import se.acrend.christopher.android.model.MessageWrapper.TicketType;
import se.acrend.christopher.android.util.DateUtil;

public class SmsTicketParser extends MessageParserBase implements MessageParser {

  // 11 jan kl 16:24
  // +'220572436'+
  // +'903765246'+
  // +'373740923'+
  // +'692092924'+
  // ÅRSKORT GULD
  // JOHAN KINDGREN
  // Avg. Norrköping C 16.24
  // Ank. Stockholm C 17.39
  // Tåg: 538
  // VU, 1 klass Kan återbetalas
  // Vagn 2, plats 30
  // Personlig biljett giltig med ID
  // Internet/Bilj.nr. SPG9352F0002
  // 010 624 472 391 895 723 215

  private DateFormat format = null;

  public SmsTicketParser() {
    super();
    format = DateUtil.createDateFormat("yyyydd MMMHH.mm");
  }

  @Override
  public boolean supports(final String message) {
    return message.contains("+'") && message.contains("'+") && message.contains("Tåg:");
  }

  @Override
  public MessageWrapper parse(final String message) {
    MessageWrapper ticket = new MessageWrapper(TicketType.SmsTicket);

    String date = findValue(message, "(\\d{1,2} \\D{3}) kl .*", "datum");

    String from = findValue(message, "Avg. (.*) \\d{1,2}\\.", "avreseort");
    String fromTime = findValue(message, "Avg. .* (\\d{1,2}\\.\\d{2})", "avgångstid");

    String to = findValue(message, "Ank. (.*) \\d{1,2}\\.", "ankomstort");
    String toTime = findValue(message, "Ank. .* (\\d{1,2}\\.\\d{2})", "ankomsttid");
    try {
      Calendar departure = createCalendar(date, fromTime);
      ticket.setFrom(from);
      ticket.setDeparture(departure);

      Calendar arrival = createCalendar(date, toTime);
      if (arrival.before(departure)) {
        arrival.add(Calendar.DAY_OF_YEAR, 1);
      }

      ticket.setTo(to);
      ticket.setArrival(arrival);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Kunde inte tolka datum. Meddelande: " + message, e);
    }
    String train = findValue(message, "T[å|a]g: (\\d*)", "tågnr");
    ticket.setTrain(train);

    String car = findValue(message, "Vagn (\\d*),", "vagn");
    String seat = findValue(message, "plats (\\d*)", "plats");

    if ((car != null) && (car.length() > 0)) {
      ticket.setCar(car);
    }
    if ((seat != null) && (seat.length() > 0)) {
      ticket.setSeat(seat);
    }

    ticket.setCode(findValue(message, "Bilj.nr. (\\D{3}\\d{4}\\D\\d{4})", "Biljettkod"));

    ticket.setMessage(message);

    ticket.validate();

    return ticket;
  }

  private Calendar createCalendar(final String date, final String time) {
    Calendar cal = Calendar.getInstance();
    Calendar now = Calendar.getInstance();
    int currentYear = now.get(Calendar.YEAR);
    try {
      cal.setTime(format.parse(currentYear + date + time));
      // Om biljetten utfärdades före idag har vi gjort fel, lägg på ett år
      if (cal.before(now.getTime())) {
        cal.add(Calendar.YEAR, 1);
      }
    } catch (ParseException e) {
      throw new IllegalArgumentException("Kunde inte tolka datum.", e);
    }
    return cal;
  }
}
