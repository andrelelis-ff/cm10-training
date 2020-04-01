package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * CMSymbol documents are constants, used in terms of enumeration values.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMSymbol}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMSymbol extends CMLocalized {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSymbol'.
   */
  String NAME = "CMSymbol";

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMSymbol>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMSymbol>> getAspects();

  /**
   * Name of the document property 'description'.
   */
  String DESCRIPTION = "description";

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  String getDescription();

  /**
   * Returns the name of this symbol.
   *
   * @return name of this symbol
   */
  String getName();

  /**
   * Name of the document property 'icon'.
   */
  String ICON = "icon";

  /**
   * Returns the value of the {@link CMSymbol} document property {@link CMSymbol#ICON}.
   *
   * @return the value of the {@link CMSymbol} document property {@link CMSymbol#ICON} or null
   */
  Blob getIcon();
}
