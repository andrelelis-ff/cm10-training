package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.logictypes.CommerceLedLinkBuilderHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Link Builder for CMChannels that are supposed to link to the commerce system.
 */
@Link
@RequestMapping
public class LiveContextChannelLinkBuilder extends LiveContextPageHandlerBase {

  private CommerceLedLinkBuilderHelper commerceLedLinkBuilderHelper;
  private SearchLandingPagesLinkBuilderHelper searchLandingPagesLinkBuilderHelper;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Required
  public void setSearchLandingPagesLinkBuilderHelper(
          SearchLandingPagesLinkBuilderHelper searchLandingPagesLinkBuilderHelper) {
    this.searchLandingPagesLinkBuilderHelper = searchLandingPagesLinkBuilderHelper;
  }

  @Required
  public void setCommerceLedLinkBuilderHelper(CommerceLedLinkBuilderHelper commerceLedLinkBuilderHelper) {
    this.commerceLedLinkBuilderHelper = commerceLedLinkBuilderHelper;
  }

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  // There is no request mapping because micro site requests are handled by the commerce system.

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @SuppressWarnings("unused")
  @Link(type = CMChannel.class)
  @Nullable
  public UriComponents buildLinkForSearchLandingPage(@NonNull CMChannel channel, @NonNull HttpServletRequest request) {
    Site site = findSite(channel).orElse(null);
    if (site == null) {
      return null;
    }

    // Search Landing Page case
    if (!commerceLedLinkBuilderHelper.isCommerceLedChannel(channel) &&
            searchLandingPagesLinkBuilderHelper.isSearchLandingPage(channel, site)) {

      CommerceConnection commerceConnection = commerceConnectionInitializer.findConnectionForSite(site).orElse(null);
      if (commerceConnection == null) {
        return null;
      }

      StoreContext storeContext = commerceConnection.getStoreContext();

      return searchLandingPagesLinkBuilderHelper
              .createSearchLandingPageURLFor(channel, commerceConnection, request, storeContext)
              .orElse(null);
    }

    return null;
  }

  @NonNull
  private Optional<Site> findSite(@NonNull CMChannel channel) {
    return getSitesService().getContentSiteAspect(channel.getContent()).findSite();
  }
}
