package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Stand-alone teaser, in case you need multiple teasers to some content,
 * so that the embedded teaser is not sufficient.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMTeaser}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMTeaser extends CMTeasable {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMTeaser'.
   */
  String NAME = "CMTeaser";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMTeaser} object
   */
  @Override
  CMTeaser getMaster();

  @Override
  Map<Locale, ? extends CMTeaser> getVariantsByLocale();

  @Override
  Collection<? extends CMTeaser> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMTeaser>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMTeaser>> getAspects();

  /**
   * Name of the document property 'target'.
   */
  String TARGET = "target";

  /**
   * Name of the document property 'targets'.
   */
  String TARGETS = "targets";

  /**
   * Returns a structure containing {@link CMLinkable} teaser targets and additional properties
   * for each target.
   *
   * <p>The structure is as follows:
   * <pre>
   *   {
   *     "links": [
   *       {
   *         "target": target1,
   *         "ctaEnabled": true,
   *         "ctaCustomText": "custom text"
   *       },
   *       ...
   *     ]
   *   }
   * </pre>
   *
   * @cm.template.api
   */
  Map<String, List<Map<String, Object>>> getTargets();
}
