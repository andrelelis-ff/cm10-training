package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMDynamicListImpl;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Generated base class for beans of document type "CMP13NSearch".
 *
 * The bean corresponding to the <code>CMP13NSearch</code> document type. It represents
 * a Solr query that may contain search functions accessing the active user's context and thus
 * contextualizing the search.
 */
public abstract class CMP13NSearchBase extends CMDynamicListImpl<CMTeasable> implements CMP13NSearch {

  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.personalization.contentbeans.CMP13NSearchImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "defaultContent"
   *
   * @return the value of the document property "defaultContent"
   */
  public List<CMTeasable> getDefaultContent() {
    List<Content> contents = getContent().getLinks("defaultContent");
    return createBeansFor(contents, CMTeasable.class);
  }

  /**
   * Returns the value of the document property "documentType"
   * @return the value of the document property "documentType"
   */
  @Override
  public String getDocumentType() {
    return getContent().getString("documentType");
  }

  /**
   * Returns the value of the document property "searchContext"
   * @return the value of the document property "searchContext"
   */
  @Override
  public List<CMNavigation> getNavigationList() {
    List<Content> contents = getContent().getLinks("searchContext");
    return createBeansFor(contents, CMNavigation.class);
  }

  /**
   * Returns the value of the document property "searchQuery"
   * @return the value of the document property "searchQuery"
   */
  @Override
  public String getSearchQuery() {
    Blob searchQueryBlob = getContent().getBlob("searchQuery");
    if (searchQueryBlob != null) {
      String charset = searchQueryBlob.getContentType().getParameter("charset");
      charset = charset == null ? "utf-8" : charset;
      return new String(searchQueryBlob.asBytes(), Charset.forName(charset));
    }
    return null;
  }

  /**
   * Returns the value of the document property "master"
   * @return the value of the document property "master"
   */
  @Override
  public CMP13NSearch getMaster() {
    return (CMP13NSearch)super.getMaster();
  }

}
