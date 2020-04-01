package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Abstract base type for navigational documents.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMContext}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMContext extends CMNavigation, HasPageGrid {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMContext'.
   */
  String NAME = "CMContext";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMContext} object
   */
  @Override
  CMContext getMaster();

  @Override
  Map<Locale, ? extends CMContext> getVariantsByLocale();

  @Override
  Collection<? extends CMContext> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMContext>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMContext>> getAspects();

}
