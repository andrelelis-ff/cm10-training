package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Actually, contexts are related to segment and link building and
 * thus belong to {@link CMLinkable}.
 * </p>
 * <p>
 * However, we want to enforce navigation trees (no arbitrary graphs),
 * therefore we don't want explicit contexts in {@link CMNavigation}
 * documents which are also derived from CMLinkable. In the content beans
 * we implement contexts in CMLinkable.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMHasContexts}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMHasContexts extends CMLinkable {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMHasContexts'.
   */
  String NAME = "CMHasContexts";

  /**
   * Name of the document property 'contexts'.
   */
  String CONTEXTS = "contexts";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMHasContexts} object
   */
  @Override
  CMHasContexts getMaster();

  @Override
  Map<Locale, ? extends CMHasContexts> getVariantsByLocale();

  @Override
  Collection<? extends CMHasContexts> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMHasContexts>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMHasContexts>> getAspects();

}
