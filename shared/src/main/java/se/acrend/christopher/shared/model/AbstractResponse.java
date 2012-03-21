package se.acrend.christopher.shared.model;

public abstract class AbstractResponse {

  private ErrorCode errorCode;
  private ReturnCode returnCode = ReturnCode.Success;
  private String informationCode;

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

  public String getInformationCode() {
    return informationCode;
  }

  public void setInformationCode(final String informationCode) {
    this.informationCode = informationCode;
  }

}