package com.coremedia.livecontext.ecommerce.ibm.common;

/**
 * Pojo generated from the json response from erroneous REST calls.
 */
@SuppressWarnings("unused") // values are injected by gson
class WcServiceError {

  private String errorCode;
  private String errorKey;
  private String errorMessage;
  private String errorParameters;

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorKey() {
    return errorKey;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getErrorParameters() {
    return errorParameters;
  }

}
