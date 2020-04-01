package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * CMViewtype symbols are used to represent viewtypes of documents.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMViewtype}.
 * </p>
 * @cm.template.api
 */
public interface CMViewtype extends CMSymbol {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMViewtype'
   */
  String NAME = "CMViewtype";

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMViewtype>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMViewtype>> getAspects();

  /**
   * Name of the document property 'layout'.
   */
  String LAYOUT = "layout";

  /**
   * Returns the value of the document property {@link #LAYOUT}.
   *
   * @return the value of the document property {@link #LAYOUT}
   * @cm.template.api
   */
  String getLayout();
}
