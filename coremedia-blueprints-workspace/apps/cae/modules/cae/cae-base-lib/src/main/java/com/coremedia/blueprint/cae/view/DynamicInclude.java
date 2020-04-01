package com.coremedia.blueprint.cae.view;

/**
 * A wrapper class for bean includes
 *
 * @cm.template.api
 */
public class DynamicInclude {

  private Object delegate;
  private String view;

  public DynamicInclude(Object delegate, String view) {
    this.delegate = delegate;
    this.view = view;
  }

  /**
   * @cm.template.api
   */
  public String getView() {
    return view;
  }

  public void setView(String view) {
    this.view = view;
  }

  public void setDelegate(Object delegate) {
    if (this.delegate == null) {
      this.delegate = delegate;
    }
  }

  /**
   * @cm.template.api
   */
  public Object getDelegate() {
    return delegate;
  }

}
