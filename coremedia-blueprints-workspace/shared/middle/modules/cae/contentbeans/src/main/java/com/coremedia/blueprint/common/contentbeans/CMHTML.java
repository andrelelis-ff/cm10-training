package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;
import com.coremedia.xml.Markup;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>
 * CMHTML beans represent static HTML code.
 * </p>
 * <p>
 * Should be used only if the concrete code snippet is given by an other party,
 * e.g. embedding of external ADs.  Your own HTML code should of course be modeled
 * by templates.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMHTML}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMHTML extends CMMedia {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMHTML'.
   */
  String NAME = "CMHTML";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMHTML} object
   */
  @Override
  CMHTML getMaster();

  @Override
  Map<Locale, ? extends CMHTML> getVariantsByLocale();

  @Override
  Collection<? extends CMHTML> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMHTML>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMHTML>> getAspects();

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
   * Returns the value of the document property {@link #DATA}.
   *
   * @return the value of the document property {@link #DATA}
   */
  @Override
  Markup getData();
}
