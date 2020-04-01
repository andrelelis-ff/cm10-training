package com.coremedia.blueprint.cae.web;

public final class IllegalRequestException extends RuntimeException {
  private String message;

  public IllegalRequestException(String message) {
    this.message = message;
  }

  public IllegalRequestException(String param, String value) {
    message = "Illegal value for request parameter \"" + param + "\": \"" + value +"\"";
  }

  @Override
  public String getMessage() {
    return message;
  }
}
