package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.blueprint.base.links.KnowsVanityUrls;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>
 * A channel is a hierarchical navigation entity with "booked content".
 * The booked content is divided into several linklists which represent
 * the areas of a page. You are encouraged to change these content linklists
 * according to your particular page layout.
 * </p>
 * <p>Some parts of a page are typically the same for many different channels,
 * e.g. headers and footers with company information, ad blocks, ...<br>
 * Therefore the implementation bean of CMChannel supports a fallback to the
 * parent channel for header and footer.
 * </p>
 * <p>
 * If a channel has several parents (i.e. referenced by the children property
 * of other channels) an arbitrary parent is chosen to inherit from. I.e.
 * channels with several parents (not recommended anyway) should bring their
 * own properties to ensure a reliable behaviour.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMChannel}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMChannel extends CMContext, KnowsVanityUrls {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMChannel'.
   */
  String NAME = "CMChannel";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMChannel} object
   */
  @Override
  CMChannel getMaster();

  @Override
  Map<Locale, ? extends CMChannel> getVariantsByLocale();

  @Override
  Collection<? extends CMChannel> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMChannel>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMChannel>> getAspects();

  /**
   * Name of the document property 'header'.
   */
  String HEADER = "header";

  /**
   * Returns the value of the document property {@link #HEADER}.
   *
   * @return a list of {@link CMLinkable} objects
   */
  List<? extends Linkable> getHeader();

  /**
   * Name of the document property 'footer'.
   */
  String FOOTER = "footer";

  /**
   * Returns the value of the document property {@link #FOOTER}.
   *
   * @return a list of {@link CMLinkable} objects
   */
  List<? extends Linkable> getFooter();


}
