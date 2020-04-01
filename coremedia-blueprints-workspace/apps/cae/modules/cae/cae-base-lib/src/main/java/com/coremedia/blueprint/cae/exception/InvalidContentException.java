package com.coremedia.blueprint.cae.exception;

public class InvalidContentException extends RuntimeException implements ExceptionRenderDynamicViewDecorator{

  private final Object invalidContent;
  private final String view;

  public InvalidContentException(String message, Object invalidContent) {
    super(message);
    this.invalidContent = invalidContent;
    this.view = null;
  }

  public InvalidContentException(String message, Object invalidContent, Exception cause) {
    super(message, cause);
    this.invalidContent = invalidContent;
    this.view = null;
  }

  public InvalidContentException(String message, Object invalidContent, String view) {
    super(message);
    this.invalidContent = invalidContent;
    this.view = view;
  }

  public Object getInvalidContent() {
    return this.invalidContent;
  }

  @Override
  public String getView() {
    return view;
  }

}
