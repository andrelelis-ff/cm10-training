package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A product teaser links to a product, which is an external object in a commerce system.
 * </p>
 * <p>
 * This content bean represents documents of that type within the CAE.
 * </p>
 *
 * @cm.template.api
 */
public interface CMProductTeaser extends CMTeasable, LiveContextProductTeasable {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMProductTeaser'.
   */
  String NAME = "CMProductTeaser";

  /**
   * Name of the document property 'externalId'.
   *
   * <p>Useful for queries and content level code.
   */
  String EXTERNAL_ID = "externalId";

  /**
   * Returns the value of the document property "master".
   *
   * @return a {@link com.coremedia.livecontext.contentbeans.CMProductTeaser} object
   */
  @Override
  CMProductTeaser getMaster();

  /**
   * Returns the variants of this {@link com.coremedia.livecontext.contentbeans.CMProductTeaser} indexed by their {@link java.util.Locale}
   *
   * @return the variants of this {@link com.coremedia.livecontext.contentbeans.CMProductTeaser} indexed by their {@link java.util.Locale}
   */
  @Override
  Map<Locale, ? extends CMProductTeaser> getVariantsByLocale();

  /**
   * Returns the {@link java.util.Locale} specific variants of this {@link com.coremedia.livecontext.contentbeans.CMProductTeaser}
   *
   * @return the {@link java.util.Locale} specific variants of this {@link com.coremedia.livecontext.contentbeans.CMProductTeaser}
   */
  @Override
  Collection<? extends CMProductTeaser> getLocalizations();

  /**
   * Returns a {@code Map} from aspectIDs to Aspects. AspectIDs consists of an aspect name with a
   * prefix which identifies the plugin provider.
   *
   * @return a {@code Map} from aspectIDs to {@code Aspect}s
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMProductTeaser>> getAspectByName();

  /**
   * Returns a list of all  {@code Aspect}s from all availiable
   * PlugIns that are registered to this content bean.
   *
   * @return a list of {@link com.coremedia.cae.aspect.Aspect}
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMProductTeaser>> getAspects();

}
