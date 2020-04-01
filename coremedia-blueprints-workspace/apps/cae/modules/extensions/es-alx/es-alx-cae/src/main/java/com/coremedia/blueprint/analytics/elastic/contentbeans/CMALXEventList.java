package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMPicture;

/**
 * Generated interface for beans of document type "CMALXEventList".
 */
public interface CMALXEventList extends CMALXBaseList<CMPicture> {

  /**
   * Returns the value of the document property "master"
   *
   * @return the value
   */
  @Override
  CMALXEventList getMaster();

  /**
   * Returns the value of the document property "category".
   *
   * @return the value
   */
  String getCategory();

  /**
   * Returns the value of the document property "action".
   *
   * @return the value
   */
  String getAction();

}