package com.coremedia.blueprint.elastic.social.cae.tenant;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.core.api.servlet.TenantLookupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;

/**
 * A {@link com.coremedia.elastic.core.api.servlet.TenantLookupStrategy} that uses the current
 * site for a request, which must have been set using the {@link SiteHelper},
 * to determine the tenant name for a given request.
 * This strategy can be used in the Blueprint CAE.
 */
@Named
public class TenantSiteLookupStrategy implements TenantLookupStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(TenantSiteLookupStrategy.class);

  @Inject
  private ElasticSocialPlugin elasticSocialPlugin;

  @Override
  public String getTenant(ServletRequest request) {
    Site siteFromRequest = SiteHelper.getSiteFromRequest(request);

    if (siteFromRequest == null) {
      LOG.trace("no navigation content found for request {}", request);
      return null;
    }

    return elasticSocialPlugin.getElasticSocialConfiguration(siteFromRequest).getTenant();
  }
}
