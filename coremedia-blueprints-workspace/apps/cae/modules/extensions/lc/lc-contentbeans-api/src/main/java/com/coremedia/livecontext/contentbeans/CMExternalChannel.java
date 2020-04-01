package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.ecommerce.common.contentbeans.CMAbstractCategory;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @cm.template.api
 */
public interface CMExternalChannel extends CMAbstractCategory {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMExternalChannel'.
   */
  String NAME = "CMExternalChannel";

  /**
   * Name of the document property 'externalId'.
   *
   * <p>Useful for queries and content level code.
   */
  String EXTERNAL_ID = "externalId";

  /**
   * Name of the 'commerce' struct in localSettings struct.
   */
  String COMMERCE_STRUCT = "commerce";

  /**
   * Name of the 'selectChildren' property in the commerce struct.
   */
  String COMMERCE_SELECT_CHILDREN = "selectChildren";

  /**
   * Name of the 'children' property in the commerce struct.
   */
  String COMMERCE_CHILDREN = "children";

  @Override
  CMExternalChannel getMaster();

  @Override
  Map<Locale, ? extends CMExternalChannel> getVariantsByLocale();

  @Override
  Collection<? extends CMExternalChannel> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMExternalChannel>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMExternalChannel>> getAspects();

  /**
   * <p>
   * Returns the content property {@link #EXTERNAL_ID}. The returned value is the full commerce id including
   * prefix for vendor and bean type.
   * </p>
   * Note that {@link CommerceBean#getExternalId()} is the unprefixed ID format.
   *
   * @return the formatted commerce id
   */
  String getExternalId();

  /**
   * Returns the category.
   *
   * @return the category
   * @cm.template.api
   */
  Category getCategory();

  PageGrid getPdpPagegrid();

  boolean isCatalogRoot();

  List<String> getCommerceChildrenIds();

  boolean isCommerceChildrenSelected();

}
