package se.acrend.christopher.shared.model;


public abstract class AbstractResponse {

  private ErrorCode errorCode;
  private ReturnCode returnCode = ReturnCode.Success;

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(final ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public ReturnCode getReturnCode() {
    return returnCode;
  }

  public void setReturnCode(final ReturnCode returnCode) {
    this.returnCode = returnCode;
  }

}