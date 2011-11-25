package se.acrend.christopher.shared.exception;

public class PermanentException extends RuntimeException {

  public PermanentException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public PermanentException(final String message) {
    super(message);
  }

  public PermanentException(final Throwable cause) {
    super(cause);
  }

}
