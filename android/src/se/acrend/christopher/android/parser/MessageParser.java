package se.acrend.christopher.android.parser;

import se.acrend.christopher.android.model.MessageWrapper;

public interface MessageParser {

  boolean supports(final String message);

  MessageWrapper parse(final String message);

}