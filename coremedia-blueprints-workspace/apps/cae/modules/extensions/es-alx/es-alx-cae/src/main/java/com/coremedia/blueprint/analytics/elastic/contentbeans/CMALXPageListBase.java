package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.cap.content.Content;

import java.util.List;

/**
 * Generated base class for beans of document type "CMALXPageList".
 */
public abstract class CMALXPageListBase extends CMALXBaseListImpl<CMLinkable> implements CMALXPageList {

  /**
   * Returns the value of the document property "documentType"
   *
   * @return the value of the document property "documentType"
   */
  @Override
  public String getDocumentType() {
    return getContent().getString(DOCUMENT_TYPE);
  }

  /**
   * Returns the value of the document property "master"
   *
   * @return the value of the document property "master"
   */
  @Override
  public CMALXPageList getMaster() {
    return (CMALXPageList) super.getMaster();
  }

  @Override
  public CMNavigation getBaseChannel() {
    final List<Content> contents = getContent().getLinks(BASE_CHANNEL);
    return contents.isEmpty() ? null : createBeanFor(contents.get(0), CMNavigation.class);
  }

  /**
   * Returns the value of the document property "defaultContent"
   *
   * @return the value of the document property "defaultContent"
   */
  @Override
  public List<CMLinkable> getDefaultContent() {
    final List<Content> contents = getContent().getLinks(DEFAULT_CONTENT);
    return createBeansFor(contents, CMLinkable.class);
  }

}
