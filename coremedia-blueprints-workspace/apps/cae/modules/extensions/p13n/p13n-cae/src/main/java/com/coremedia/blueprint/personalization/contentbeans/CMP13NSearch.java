package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDynamicList;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;

import java.util.List;

/**
 * <p>
 * Generated interface for beans of document type "CMP13NSearch".
 * </p>
 * <p>
 * The bean corresponding to the {@code CMP13NSearch} document type. It represents
 * a Solr query that may contain search functions accessing the active user's context and thus
 * contextualizing the search.
 * </p>
 *
 * @cm.template.api
 */
public interface CMP13NSearch extends CMDynamicList<CMTeasable> {

  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.personalization.contentbeans.CMP13NSearchImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMP13NSearch'
   */
  String CONTENTTYPE_CMP13NSEARCH = "CMP13NSearch";

  /**
   * Returns the value of the document property "documentType"
   *
   * @return the value
   */
  String getDocumentType();

  /**
   * Returns the value of the document property "searchContext"
   *
   * @return the value of the document property "searchContext"
   */
  List<CMNavigation> getNavigationList();

  /**
   * Returns the value of the document property "searchQuery"
   *
   * @return the value
   */
  String getSearchQuery();

  /**
   * Provides access to the status produced while executing {@link #getItems()} as a dictionary in JSON notation.
   * Intended to be included in an HTML (preview) page so it can be picked up by CoreMedia Studio components.
   *
   * @return the current status or {@code null}
   * @cm.template.api
   */
  String getSearchStatusAsJSON();

  /**
   * Returns the value of the document property "master"
   *
   * @return the value
   */
  @Override
  CMP13NSearch getMaster();
}
