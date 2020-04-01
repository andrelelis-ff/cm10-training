package com.coremedia.blueprint.common.navigation;

/**
 * This marker interface provides a simple means to support custom view types for beans.
 * For alternatives, ... todo provide alternatives to implementing this interface in ViewTypeRenderNodeDecorator, e.g. some CustomViewTypeRovider?
 */

public interface HasViewTypeName {

  /**
   * Return a view type name to be used for this bean, or null if there is no special view type associated with it
   */
  String getViewTypeName();

}
