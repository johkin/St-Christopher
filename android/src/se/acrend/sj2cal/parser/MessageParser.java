package se.acrend.sj2cal.parser;


public interface MessageParser {

  boolean supports(final String message);

  MessageWrapper parse(final String message);

}