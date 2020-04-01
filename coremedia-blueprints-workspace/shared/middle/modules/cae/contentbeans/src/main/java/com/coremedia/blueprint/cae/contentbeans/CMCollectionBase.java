package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMCollection.
 * Should not be changed.
 */
public abstract class CMCollectionBase<T> extends CMTeasableImpl implements CMCollection<T> {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMCollection} objects
   */
  @Override
  @SuppressWarnings("unchecked")
  public CMCollection<T> getMaster() {
    return (CMCollection<T>) super.getMaster();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Locale, ? extends CMCollection<T>> getVariantsByLocale() {
    return (Map<Locale, ? extends CMCollection<T>>) super.getVariantsByLocale();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMCollection<T>> getLocalizations() {
    return (Collection<? extends CMCollection<T>>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMCollection<T>>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMCollection<T>>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMCollection<T>>> getAspects() {
    return (List<? extends Aspect<? extends CMCollection<T>>>) super.getAspects();
  }

  @Override
  public List<T> getItems() {
    return filterItems(getItemsUnfiltered());
  }

  /**
   * Filter items using the {@link #getValidationService()}
   * @param itemsUnfiltered the list of unfiltered items (not necessarily instances of CMLinkable)
   * @return a list of items that have passed validation
   */
  @SuppressWarnings("unchecked")
  protected List<T> filterItems(List<T> itemsUnfiltered) {
    ValidationService<T> validationService = (ValidationService<T>) getValidationService();
    return (List<T>) validationService.filterList(itemsUnfiltered);
  }

  @SuppressWarnings("unchecked")
  public List<T> getItemsUnfiltered() {
    List<Content> contents = getContent().getLinks(CMCollection.ITEMS);
    return (List<T>) createBeansFor(contents, ContentBean.class);
  }

}
