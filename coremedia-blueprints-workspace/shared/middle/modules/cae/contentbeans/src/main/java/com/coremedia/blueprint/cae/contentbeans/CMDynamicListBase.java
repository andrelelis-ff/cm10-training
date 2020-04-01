package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDynamicList;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Generated base class for immutable beans of document type CMDynamicList.
 * Should not be changed.
 */
public abstract class CMDynamicListBase<T> extends CMCollectionImpl<T> implements CMDynamicList<T> {

  @Override
  @SuppressWarnings("unchecked")
  public CMDynamicList<T> getMaster() {
    return (CMDynamicList<T>) super.getMaster();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Locale, ? extends CMDynamicList<T>> getVariantsByLocale() {
    return (Map<Locale, ? extends CMDynamicList<T>>) super.getVariantsByLocale();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMDynamicList<T>> getLocalizations() {
    return (Collection<? extends CMDynamicList<T>>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMDynamicList<T>>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMDynamicList<T>>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMDynamicList<T>>> getAspects() {
    return (List<? extends Aspect<? extends CMDynamicList<T>>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #MAX_LENGTH}.
   *
   * @return Maximum number of entries in the dynamic content list.
   *         The list may return fewer entries. A value of 0 or less indicates no restriction.
   */
  @Override
  public int getMaxLength() {
    return getContent().getInt(MAX_LENGTH);
  }

  /**
   * Returns the number of items in this list.
   *
   * @return the number of items in this list, must match {@link #getItems}().size().
   */
  @Override
  public int getLength() {
    return getItems().size();
  }
}
