package com.coremedia.ecommerce.studio.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Strategy interface to resolve content for a given commerce shop url
 */
public interface PbeShopUrlTargetResolver {

  @Nullable
  Object resolveUrl(@NonNull String urlStr, @Nullable String siteId);

}
