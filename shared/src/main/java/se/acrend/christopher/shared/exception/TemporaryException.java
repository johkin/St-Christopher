package se.acrend.christopher.shared.exception;

public class TemporaryException extends RuntimeException {

  public TemporaryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public TemporaryException(final String message) {
    super(message);
  }

  public TemporaryException(final Throwable cause) {
    super(cause);
  }

}
