package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Suitable for URLs whose second segment is a content id, e.g. /rest/1234/...
 */
public class RestCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  @NonNull
  @Override
  protected Optional<Site> findSite(HttpServletRequest request, String normalizedPath) {
    Site site = getSiteResolver().findSiteForPathWithContentId(normalizedPath);
    return Optional.ofNullable(site);
  }
}
