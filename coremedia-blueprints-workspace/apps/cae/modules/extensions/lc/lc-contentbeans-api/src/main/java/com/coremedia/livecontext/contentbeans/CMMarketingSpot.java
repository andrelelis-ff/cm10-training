package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDynamicList;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A marketing spot is an external object, which is used within a commerce system, to define
 * some kind of rules for rendering dynamic, personalized content.
 * </p>
 * <p>
 * Because a marketeer is able to link to marketing spots from within the CoreMedia content
 * and to place marketing spots on pages, he needs some kind of representation. Such a
 * representation is a document of type {@link #NAME CMMarketingSpot}.
 * </p>
 * <p>
 * This content bean represents documents of that type within the CAE.
 * </p>
 *
 * @cm.template.api
 */
public interface CMMarketingSpot extends CMDynamicList {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMMarketingSpot'.
   */
  String NAME = "CMMarketingSpot";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMMarketingSpot} object
   */
  @Override
  CMMarketingSpot getMaster();

  /**
   * Returns the variants of this {@link CMMarketingSpot} indexed by their {@link Locale}
   *
   * @return the variants of this {@link CMMarketingSpot} indexed by their {@link Locale}
   */
  @Override
  Map<Locale, ? extends CMMarketingSpot> getVariantsByLocale();

  /**
   * Returns the {@link Locale} specific variants of this {@link CMMarketingSpot}
   *
   * @return the {@link Locale} specific variants of this {@link CMMarketingSpot}
   */
  @Override
  Collection<? extends CMMarketingSpot> getLocalizations();

  /**
   * Returns a {@code Map} from aspectIDs to Aspects. AspectIDs consists of an aspect name with a
   * prefix which identifies the plugin provider.
   *
   * @return a {@code Map} from aspectIDs to {@code Aspect}s
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMMarketingSpot>> getAspectByName();

  /**
   * Returns a list of all  {@code Aspect}s from all availiable
   * PlugIns that are registered to this content bean.
   *
   * @return a list of {@link com.coremedia.cae.aspect.Aspect}
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMMarketingSpot>> getAspects();

  /**
   * Returns an external primary key representing the e-marketing spot.
   *
   * @return an external primary key
   */
  String getExternalId();

  /**
   * @return list of items calculated by the marketing spot
   */
  @Override
  List getItems();

  /**
   * Returns the commerce marketing spot
   *
   * @cm.template.api
   */
  @Nullable
  MarketingSpot getMarketingSpot();

}
