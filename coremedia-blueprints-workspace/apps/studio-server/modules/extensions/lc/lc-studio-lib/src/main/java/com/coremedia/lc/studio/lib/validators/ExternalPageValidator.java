package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.validation.Issues;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import static com.coremedia.rest.validation.Severity.ERROR;

public class ExternalPageValidator extends CatalogLinkValidator {

  private static final String CODE_ISSUE_EXTERNAL_PAGE_ID_EMPTY = "EmptyExternalPageId";

  public ExternalPageValidator(CommerceConnectionInitializer commerceConnectionInitializer,
                               SitesService sitesService,
                               String propertyName) {
    super(commerceConnectionInitializer, sitesService, propertyName);
  }

  @Override
  protected void emptyPropertyValue(@NonNull Content content, @NonNull Issues issues) {
    Content siteRootDocument = getSiteRootDocument(content);

    // Only add issue if content is not the site root document.
    // Site root document can have an empty external id.
    if (siteRootDocument == null || siteRootDocument.equals(content)) {
      return;
    }

    addIssue(issues, ERROR, CODE_ISSUE_EXTERNAL_PAGE_ID_EMPTY);
  }

  @Nullable
  private Content getSiteRootDocument(@NonNull Content content) {
    Site site = getSite(content);
    if (site == null) {
      return null;
    }

    return site.getSiteRootDocument();
  }

  @Override
  protected void invalidExternalId(Issues issues, Object... arguments) {
    // validation for external id not applicable for external pages
  }
}
