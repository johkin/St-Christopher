package se.acrend.christopher.shared.model;

import javax.xml.bind.annotation.XmlElement;

public abstract class AbstractResponse {

  private ErrorCode errorCode;
  private ReturnCode returnCode = ReturnCode.Success;

  @XmlElement(name = "errorCode")
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(final ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  @XmlElement(name = "returnCode")
  public ReturnCode getReturnCode() {
    return returnCode;
  }

  public void setReturnCode(final ReturnCode returnCode) {
    this.returnCode = returnCode;
  }

}