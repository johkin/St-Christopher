package se.acrend.christopher.android.billing.util;

/**
 * Exception thrown when encountering an invalid Base64 input character.
 * 
 * @author nelson
 */
public class Base64DecoderException extends Exception {
  public Base64DecoderException() {
    super();
  }

  public Base64DecoderException(final String s) {
    super(s);
  }

  private static final long serialVersionUID = 1L;
}
