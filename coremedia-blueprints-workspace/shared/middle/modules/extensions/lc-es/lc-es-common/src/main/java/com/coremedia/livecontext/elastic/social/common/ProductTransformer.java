package com.coremedia.livecontext.elastic.social.common;

import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetTransformer;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.ProductInSiteImpl;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Objects.requireNonNull;

@Named
class ProductTransformer implements ContributionTargetTransformer<Product, ProductInSite> {

  @Inject
  private SitesService sitesService;

  @Override
  @NonNull
  public ProductInSite transform(@NonNull Product target) {
    StoreContext context = target.getContext();

    String siteId = requireNonNull(context.getSiteId(), "Site ID must be set on store context.");
    Site site = sitesService.findSite(siteId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown site ID '%s'.", siteId)));

    return new ProductInSiteImpl(target, site);
  }

  @Override
  @Nullable
  public Site getSite(@NonNull Product target) {
    String siteId = target.getContext().getSiteId();
    return sitesService.findSite(siteId).orElse(null);
  }

  @Override
  @NonNull
  public Class<Product> getType() {
    return Product.class;
  }
}
