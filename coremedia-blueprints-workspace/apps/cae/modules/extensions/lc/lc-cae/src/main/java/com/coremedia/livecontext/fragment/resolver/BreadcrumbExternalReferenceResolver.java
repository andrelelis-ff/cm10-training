package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.resolver.ContentSeoSegmentExternalReferenceResolver.Ids;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

/**
 * Resolves the breadcrumb for full page layouts in the commerce led scenario.
 */
public class BreadcrumbExternalReferenceResolver extends ExternalReferenceResolverBase {
  private static final String PREFIX = "cm-breadcrumb";

  public BreadcrumbExternalReferenceResolver() {
    super(PREFIX);
  }

  // --- properties --------------------------------------------------

  private String storefrontUrl;
  private boolean lowerCaseSiteName = true;

  @Required
  public void setStorefrontUrl(String storefrontUrl) {
    this.storefrontUrl = storefrontUrl;
  }

  public void setLowerCaseSiteName(boolean lowerCaseSiteName) {
    this.lowerCaseSiteName = lowerCaseSiteName;
  }

  // --- interface --------------------------------------------------
  @Override
  protected boolean include(@NonNull FragmentParameters fragmentParameters, @NonNull String referenceInfo) {
    return true;
  }

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    //no SEO segment passed, so we only render the root channel which results in an empty breadcrumb
    if (fragmentParameters.getParameter() == null) {
      Content channel = site.getSiteRootDocument();
      return new LinkableAndNavigation(channel, channel);
    }

    //regular breadcrumb building using the SEO segment instead
    String parameter = fragmentParameters.getParameter();
    Ids ids = ContentSeoSegmentExternalReferenceResolver.parseExternalReferenceInfo(parameter);

    String contentId = IdHelper.formatContentId(ids.contentId);
    Content linkable = contentRepository.getContent(contentId);

    Content channel;
    if (ids.contextId != null) {
      String contextId = IdHelper.formatContentId(ids.contextId);
      channel = contentRepository.getContent(contextId);
    } else {
      channel = linkable;
    }

    //use the fragment parameters locale, not the one of the site
    String storeName = CurrentStoreContext.get().getStoreName();
    if (lowerCaseSiteName) {
      storeName = storeName.toLowerCase(fragmentParameters.getLocale());
    }
    String homepageUrl = storefrontUrl + fragmentParameters.getLocale().getLanguage() + "/" + storeName;
    fragmentParameters.setParameter(homepageUrl);

    return new LinkableAndNavigation(linkable, channel);
  }
}
