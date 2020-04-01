package com.coremedia.blueprint.ecommerce.cae;

import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Suitable for URLs whose second segment denotes the site, e.g. /helios/...
 */
public class WebCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  @NonNull
  @Override
  protected Optional<Site> findSite(HttpServletRequest request, String normalizedPath) {
    Site site = getSiteResolver().findSiteByPath(normalizedPath);
    return Optional.ofNullable(site);
  }
}
