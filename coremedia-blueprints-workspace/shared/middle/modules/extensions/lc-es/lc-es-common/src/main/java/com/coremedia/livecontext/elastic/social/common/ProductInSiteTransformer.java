package com.coremedia.livecontext.elastic.social.common;

import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetTransformer;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Named;

@Named
public class ProductInSiteTransformer implements ContributionTargetTransformer<ProductInSite,ProductInSite> {

  @NonNull
  @Override
  public ProductInSite transform(@NonNull ProductInSite target) {
    return target;
  }

  @Nullable
  @Override
  public Site getSite(@NonNull ProductInSite target) {
    return target.getSite();
  }

  @NonNull
  @Override
  public Class<ProductInSite> getType() {
    return ProductInSite.class;
  }
}
