package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Common Interface for {@link com.coremedia.livecontext.contentbeans.CMProductTeaser} and
 * {@link com.coremedia.livecontext.contentbeans.LiveContextExternalProduct} to share same templates.
 *
 * @cm.template.api
 */
public interface LiveContextProductTeasable extends CMTeasable {

  /**
   * Returns the teasered product.
   *
   * @return the teasered product
   * @cm.template.api
   */
  @Nullable
  Product getProduct();

  /**
   * Returns the underlying Product in this content's site for link building.
   * <p>
   * You cannot build links for a Product, since a Product can occur in multiple
   * sites and is thus not unique for link building.
   * Use a {@link ProductInSite} for link building.
   *
   * @return a ProductInSite or null if product or site cannot be determined.
   * @cm.template.api
   */
  @Nullable
  ProductInSite getProductInSite();

  /**
   * Returns true if the "Shop now" visualization is to be applied on this
   * teaser.
   *
   * @param context fallback to lookup the shop now policy
   * @cm.template.api
   */
  boolean isShopNowEnabled(CMContext context);


  /**
   * <p>
   * Returns the content property {@link CMProductTeaser#EXTERNAL_ID}. The returned value is the full commerce id including
   * prefix for vendor and bean type.
   * </p>
   * Note that {@link CommerceBean#getExternalId()} is the unprefixed ID format.
   *
   * @return the formatted commerce id
   * @cm.template.api
   */
  @NonNull
  String getExternalId();
}
