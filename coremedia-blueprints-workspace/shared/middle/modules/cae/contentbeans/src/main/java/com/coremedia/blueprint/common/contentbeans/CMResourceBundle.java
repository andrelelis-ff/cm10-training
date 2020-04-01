package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Represents document type {@link #NAME CMResourceBundle}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMResourceBundle extends CMLocalized {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMResourceBundle'.
   */
  String NAME = "CMResourceBundle";

  /**
   * Name of the document property 'localizations'.
   */
  String LOCALIZATIONS = "localizations";

  @Override
  CMResourceBundle getMaster();

  @Override
  Map<Locale, ? extends CMResourceBundle> getVariantsByLocale();

  @Override
  Collection<? extends CMResourceBundle> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMResourceBundle>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMResourceBundle>> getAspects();

  /**
   * Returns the value of the document property {@link #LOCALIZATIONS}.
   *
   * @return a struct of localization objects
   */
  Struct getLocalizationStruct();
}
