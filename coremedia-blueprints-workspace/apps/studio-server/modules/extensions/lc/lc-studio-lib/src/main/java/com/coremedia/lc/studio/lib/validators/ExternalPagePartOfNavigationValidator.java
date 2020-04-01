package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.rest.validators.ChannelIsPartOfNavigationValidator;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;

/**
 * Validates if link list properties of the given content do reference themselves.
 */
public class ExternalPagePartOfNavigationValidator extends ChannelIsPartOfNavigationValidator {

  private SitesService sitesService;

  public ExternalPagePartOfNavigationValidator(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Override
  protected boolean isNotInNavigation(Content content) {
    Site site = sitesService.getContentSiteAspect(content).getSite();
    boolean notInNavigation = super.isNotInNavigation(content);
    // check if the preferred site exists for this content and if teh external page is in navigation of this site
    return (site == null) || notInNavigation && !content.equals(site.getSiteRootDocument());
  }
}
