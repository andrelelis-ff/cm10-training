package com.coremedia.livecontext.commercebeans;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A product can occur in several sites.
 * ProductInSite associates a product with a site for deterministic link building.
 *
 * @cm.template.api
 */
public interface ProductInSite extends CommerceObject {
  /**
   * Returns the product.
   *
   * @return the product
   * @cm.template.api
   */
  @NonNull
  Product getProduct();

  /**
   * Returns the site.
   *
   * @return the site.
   */
  @NonNull
  Site getSite();
}
