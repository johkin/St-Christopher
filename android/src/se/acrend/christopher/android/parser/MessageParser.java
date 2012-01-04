package se.acrend.christopher.android.parser;

import se.acrend.christopher.android.model.MessageWrapper;

public interface MessageParser {

  boolean supports(String sender, String message);

  MessageWrapper parse(String message);

}