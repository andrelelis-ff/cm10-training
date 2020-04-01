package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;
import com.coremedia.xml.Markup;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>
 * Aka ClientCode. E.g. for CSS or JS.
 * We represent script code as CoreMedia Richtext because of
 * internal link support.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMAbstractCode}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMAbstractCode extends CMLocalized {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMAbstractCode'.
   */
  String NAME = "CMAbstractCode";


  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMAbstractCode} object
   */
  @Override
  CMAbstractCode getMaster();

  @Override
  Map<Locale, ? extends CMAbstractCode> getVariantsByLocale();

  @Override
  Collection<? extends CMAbstractCode> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMAbstractCode>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMAbstractCode>> getAspects();

  /**
   * Name of the document property 'description'.
   */
  String DESCRIPTION = "description";

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   * @cm.template.api
   */
  String getDescription();


  /**
   * Name of the document property 'code'.
   */
  String CODE = "code";

  /**
   * Returns the value of the document property {@link #CODE}.
   *
   * @return the value of the document property {@link #CODE}
   * @cm.template.api
   */
  Markup getCode();


  /**
   * Name of the document property 'include'.
   */
  String INCLUDE = "include";

  /**
   * Returns the value of the document property {@link #INCLUDE}.
   *
   * @return a list of {@link CMAbstractCode} objects
   * @cm.template.api
   */
  @NonNull
  List<? extends CMAbstractCode> getInclude();

  /**
   * @return the content type of the code, e.g. text/css
   * @cm.template.api
   */
  String getContentType();

  /**
   * Name of the document property 'ieExpression'.
   */
  String IE_EXPRESSION = "ieExpression";

  /**
   * Returns the value of the document property {@link #IE_EXPRESSION}.
   *
   * @return the value of the document property {@link #IE_EXPRESSION}
   * @cm.template.api
   */
  String getIeExpression();

  /**
   * Name of the document property 'ieRevealed'.
   */
  String IE_REVEALED = "ieRevealed";

  /**
   * Returns the value of the document property {@link #IE_REVEALED}.
   *
   * @return the value of the document property {@link #IE_REVEALED}
   * @cm.template.api
   */
  boolean isIeRevealed();

  /**
   * Name of the document property 'dataUrl'.
   */
  String DATA_URL = "dataUrl";

  /**
   * Returns the value of the document property {@link #DATA_URL}.
   *
   * @return the value of the document property {@link #DATA_URL}
   * @cm.template.api
   */
  String getDataUrl();

  /**
   * Name of the document property 'htmlAttributes'.
   */
  String HTML_ATTRIBUTES = "htmlAttributes";

  /**
   * Returns the value of the document property {@link #HTML_ATTRIBUTES}.
   */
  @NonNull
  Map<String, Object> getHtmlAttributes();

  /**
   * Name of the document property 'disableCompress'.
   *
   * @deprecated  We will be removing the compression of code from the CAE as the frontend workspace provides
   *              options to compress the code before it is uploaded to the content repository.
   */
  @Deprecated
  String DISABLE_COMPRESSION = "disableCompress";

  /**
   * Returns the value of the document property {@link #DISABLE_COMPRESSION}.
   *
   * @return the value of the document property {@link #DISABLE_COMPRESSION}
   *
   * @deprecated We will be removing the compression of code from the CAE as the frontend workspace provides
   *             options to compress the code before it is uploaded to the content repository.
   */
  @Deprecated
  boolean isCompressionDisabled();
}
