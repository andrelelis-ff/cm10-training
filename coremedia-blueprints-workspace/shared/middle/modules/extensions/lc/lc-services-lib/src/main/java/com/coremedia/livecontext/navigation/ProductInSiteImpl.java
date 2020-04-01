package com.coremedia.livecontext.navigation;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Immutable instances of ProductInSite.
 */
public class ProductInSiteImpl implements ProductInSite {
  private final Product product;
  private final Site site;

  public ProductInSiteImpl(@NonNull Product product, @NonNull Site site) {
    this.product = product;
    this.site = site;
  }

  @NonNull
  @Override
  public Product getProduct() {
    return product;
  }

  @NonNull
  @Override
  public Site getSite() {
    return site;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ProductInSiteImpl that = (ProductInSiteImpl) o;

    if (!product.equals(that.product)) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (!site.equals(that.site)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = product.hashCode();
    result = 31 * result + (site.hashCode());
    return result;
  }
}
