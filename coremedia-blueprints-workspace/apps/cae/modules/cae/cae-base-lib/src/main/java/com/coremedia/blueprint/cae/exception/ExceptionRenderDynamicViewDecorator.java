package com.coremedia.blueprint.cae.exception;

/**
 * Decorator interface to enable rendering catched exceptions with a custom ^view.
 */
public interface ExceptionRenderDynamicViewDecorator {

  /**
   * provides the view, which should be used for rendering. set null for
   * default view.
   * @return the view
   */
  String getView();

}
