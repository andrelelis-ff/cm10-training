package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A placeholder for a dynamically populated list of content objects.
 * </p>
 * <p>
 * The actual entries of the dynamic list depend on the concrete subtype and
 * the plug-in implementation reponsible for providing the list entries.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMDynamicList}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMDynamicList<T> extends CMCollection<T> {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMDynamicList'.
   */
  String NAME = "CMDynamicList";

  @Override
  CMDynamicList<T> getMaster();

  @Override
  Map<Locale, ? extends CMDynamicList<T>> getVariantsByLocale();

  @Override
  Collection<? extends CMDynamicList<T>> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMDynamicList<T>>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMDynamicList<T>>> getAspects();

  /**
   * Name of the document property 'maxLength'.
   */
  String MAX_LENGTH = "maxLength";

  /**
   * Returns the value of the document property {@link #MAX_LENGTH}.
   *
   * @return Maximum number of entries in the dynamic content list.
   * The list may return fewer entries. A value of 0 or less indicates no restriction.
   */
  int getMaxLength();

  /**
   * Returns the number of items in this list.
   *
   * @return the number of items in this list, must match {@link #getItems}().size().
   */
  int getLength();
}
