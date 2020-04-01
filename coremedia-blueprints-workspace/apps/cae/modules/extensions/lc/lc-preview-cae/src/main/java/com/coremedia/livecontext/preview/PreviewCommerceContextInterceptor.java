package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Creates the store context for preview urls.
 */
public class PreviewCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  private SitesService sitesService;

  @NonNull
  @Override
  protected Optional<Site> findSite(HttpServletRequest request, String normalizedPath) {
    Site site = null;

    String[] siteIds = request.getParameterMap().get("site");
    if (siteIds != null && siteIds.length == 1) {
      site = sitesService.getSite(siteIds[0]);
    }

    if (site == null) {
      String[] ids = request.getParameterMap().get("id");
      if (ids != null && ids.length > 0) {
        String id = ids[0];
        if (IdHelper.isContentId(id) || StringUtils.isNumeric(id)) {
          int contentId = IdHelper.parseContentId(id);
          site = getSiteResolver().findSiteForContentId(contentId);
        }
      }
    }

    return Optional.ofNullable(site);
  }

  @Required
  public void setSitesService(SitesService siteService) {
    this.sitesService = siteService;
  }
}
