package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriod;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.cae.aspect.Aspect;

import java.util.Locale;
import java.util.Map;

/**
 * An abstraction from full Pages and Page Fragments.
 *
 * @cm.template.api
 */
public interface AbstractPage extends ValidityPeriod {

  /**
   * Returns the {@link Navigation navigation} denoted by the uri of the request for this response which
   * is not necessarily a content bean.
   *
   * @return The navigation instance of this page.
   * @cm.template.api
   */
  Navigation getNavigation();

  /**
   * Returns the main content object rendered on this page
   *
   * @cm.template.api
   */
  Object getContent();

  /**
   * Returns true, if the CAE that rendered this response runs in development mode.
   *
   * @cm.template.api
   */
  boolean isDeveloperMode();

  /**
   * Returns a {@link PageGrid}
   *
   * @cm.template.api
   */
  PageGrid getPageGrid();

  /**
   * Returns the {@code Locale} for this abstract page.
   *
   * @cm.template.api
   */
  Locale getLocale();

  /**
   * Returns a {@link java.lang.String} "ltr" or "rtl" depending to the {@code Locale} for this page
   *
   * @cm.template.api
   */
  String getDirection();

  /**
   * Returns the title for this page
   *
   * @cm.template.api
   */
  String getTitle();

  /**
   * Returns the keywords for this page
   *
   * @cm.template.api
   */
  String getKeywords();

  /**
   * Returns the description for this page
   *
   * @deprecated use {@link com.coremedia.blueprint.common.contentbeans.CMLinkable#getHtmlDescription instead}
   */
  @Deprecated
  String getDescription();

  /**
   * A page is considered to be a detail view if the main contend rendered on the page is not a navigation node.
   *
   * @return true if if the {@link #getContent()} main content} of this page is not {@link com.coremedia.blueprint.common.navigation.Navigation} instance.
   * @cm.template.api
   */
  boolean isDetailView();

  /**
   * Returns a {@code Map} from aspectIDs to {@code Aspects}. AspectIDs consists of an aspect name
   * with a prefix which identifies the plugin provider.
   *
   * @return a {@code Map} from aspectIDs to {@code Aspect}s
   * @cm.template.api
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  Map<String, ? extends Aspect> getAspectByName();

  /**
   * Returns an id of the main content rendered on this page, e.g. for analytics purposes. May be null.
   *
   * @cm.template.api
   */
  String getContentId();

  /**
   * Returns an optional type or classifier name of the main content rendered on this page, e.g. for analytics purposes.
   * May be null.
   *
   * @cm.template.api
   */
  String getContentType();
}
