package com.stubhub.domain.inventory.listings.v2.newflow.exception;

import com.stubhub.common.exception.ErrorType;

public class ListingException extends RuntimeException {
  private static final long serialVersionUID = 5864129868366426758L;

  private ErrorType type;
  private ErrorCodeEnum errorCodeEnum;
  private String customMessage;

  public ListingException(ErrorType type, ErrorCodeEnum errorCodeEnum) {
    super(errorCodeEnum.getDescription());
    this.type = type;
    this.errorCodeEnum = errorCodeEnum;
  }

  public ListingException(ErrorType type, ErrorCodeEnum errorCodeEnum, String customMessage) {
    super(customMessage);
    this.type = type;
    this.errorCodeEnum = errorCodeEnum;
    this.customMessage = customMessage;
  }

  public ErrorType getType() {
    return type;
  }

  public ErrorCodeEnum getErrorCodeEnum() {
    return errorCodeEnum;
  }

  public void setErrorCodeEnum(ErrorCodeEnum errorCodeEnum) {
    this.errorCodeEnum = errorCodeEnum;
  }

  public String getCustomMessage() {
    return customMessage;
  }

  public void setCustomMessage(String customMessage) {
    this.customMessage = customMessage;
  }

}
