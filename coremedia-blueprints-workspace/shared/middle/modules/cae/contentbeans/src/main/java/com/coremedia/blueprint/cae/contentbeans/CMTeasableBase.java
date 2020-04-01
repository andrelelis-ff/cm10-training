package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.common.contentbeans.CMPerson;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Generated base class for immutable beans of document type CMTeasable.
 * Should not be changed.
 */
public abstract class CMTeasableBase extends CMHasContextsImpl implements CMTeasable {

  private SearchResultFactory resultFactory;

  public SearchResultFactory getResultFactory() {
    return resultFactory;
  }

  @Required
  public void setResultFactory(SearchResultFactory resultFactory) {
    this.resultFactory = resultFactory;
  }

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMTeasable} objects
   */
  @Override
  public CMTeasable getMaster() {
    return (CMTeasable) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMTeasable> getVariantsByLocale() {
    return getVariantsByLocale(CMTeasable.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMTeasable> getLocalizations() {
    return (Collection<? extends CMTeasable>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMTeasable>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMTeasable>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMTeasable>> getAspects() {
    return (List<? extends Aspect<? extends CMTeasable>>) super.getAspects();
  }

  @Override
  public String getTeaserTitle() {
    return getContent().getString(TEASER_TITLE);
  }

  @Override
  public Markup getTeaserText() {
    return getMarkup(TEASER_TEXT);
  }

  @Override
  public Markup getDetailText() {
    return getMarkup(DETAIL_TEXT);
  }

  @Override
  public List<CMMedia> getMedia() {
    List<Content> contents = getContent().getLinks(PICTURES);
    return createBeansFor(contents, CMMedia.class);
  }

  @Override
  public CMLinkable getTarget() {
    return this;
  }

  @Override
  public boolean isNotSearchable() {
    return getContent().getInt(NOT_SEARCHABLE) == 1;
  }

  @Override
  public List<? extends CMTeasable> getRelated() {
    List<Content> contents = getContent().getLinks(RELATED);
    return createBeansFor(contents, CMTeasable.class);
  }

  @Override
  public List<CMPerson> getAuthors() {
    List<Content> contents = getContent().getLinks(AUTHORS);
    return createBeansFor(contents, CMPerson.class);
  }
}
