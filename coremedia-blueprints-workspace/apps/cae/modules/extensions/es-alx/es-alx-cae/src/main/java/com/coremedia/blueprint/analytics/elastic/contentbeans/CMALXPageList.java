package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;

import java.util.List;

/**
 * Generated interface for beans of document type "CMALXPageList".
 */
public interface CMALXPageList extends CMALXBaseList<CMLinkable> {

  /**
   * Name of the document property 'documentType'.
   */
  String DOCUMENT_TYPE = "documentType";

  /**
   * Returns the value of the document property {@link #DOCUMENT_TYPE}.
   *
   * @return the value
   */
  String getDocumentType();

  /**
   * Returns the value of the document property "master"
   *
   * @return the value
   */
  @Override
  CMALXPageList getMaster();

  /**
   * Name of the document property 'baseChannel'.
   */
  String BASE_CHANNEL = "baseChannel";

  /**
   * Returns the value of the document property {@link #BASE_CHANNEL}.
   *
   * @return the value
   */
  CMNavigation getBaseChannel();

  /**
   * Name of the document property 'defaultContent'.
   */
  String DEFAULT_CONTENT = "defaultContent";

  /**
   * Returns the value of the document property {@link #DEFAULT_CONTENT}.
   *
   * @return the value
   */
  List<CMLinkable> getDefaultContent();
}