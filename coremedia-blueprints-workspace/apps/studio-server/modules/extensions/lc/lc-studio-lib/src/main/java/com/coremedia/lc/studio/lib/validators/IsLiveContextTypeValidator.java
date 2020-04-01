package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.cap.validation.ContentTypeValidatorBase;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;

/**
 * Make ssure the content type can only be used in livecontext sites
 */
public class IsLiveContextTypeValidator extends ContentTypeValidatorBase {

  private static final String CODE_ISSUE_DOC_TYPE_NOT_SUPPORTED = "DocTypeNotSupported";

  private CommerceConnectionInitializer commerceConnectionInitializer;
  private SitesService sitesService;

  public IsLiveContextTypeValidator(CommerceConnectionInitializer commerceConnectionInitializer,
                                    SitesService sitesService) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
    this.sitesService = sitesService;
  }

  @Override
  public void validate(Content content, Issues issues) {
    if (content == null || !content.isInProduction()) {
      return;
    }

    //check if the content belongs to a livecontext site
    boolean isLiveContextConnectionPresent = sitesService.getContentSiteAspect(content).findSite()
            .flatMap(commerceConnectionInitializer::findConnectionForSite)
            .filter(c -> !"coremedia".equals(c.getVendorName()))
            .isPresent();
    if (!isLiveContextConnectionPresent) {
      issues.addIssue(Severity.ERROR, null, getContentType() + "_" + CODE_ISSUE_DOC_TYPE_NOT_SUPPORTED, getContentType());
    }
  }
}
