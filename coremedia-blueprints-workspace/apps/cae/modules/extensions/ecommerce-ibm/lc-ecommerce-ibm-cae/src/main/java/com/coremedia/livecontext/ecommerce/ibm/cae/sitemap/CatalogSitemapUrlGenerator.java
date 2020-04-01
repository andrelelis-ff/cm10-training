package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.sitemap.SitemapUrlGenerator;
import com.coremedia.blueprint.cae.sitemap.UrlCollector;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.ExternalNavigationHandler;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.livecontext.product.ProductPageHandler;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;

public class CatalogSitemapUrlGenerator implements SitemapUrlGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogSitemapUrlGenerator.class);

  private CommerceConnectionInitializer commerceConnectionInitializer;
  private LiveContextNavigationFactory liveContextNavigationFactory;
  private LinkFormatter linkFormatter;
  private SettingsService settingsService;

  // --- configuration ----------------------------------------------

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  // --- SitemapUrlGenerator ----------------------------------------

  @Override
  public void generateUrls(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Site site,
                           boolean absoluteUrls, String protocol, UrlCollector urlCollector) {
    if (site == null) {
      throw new IllegalArgumentException("Cannot derive a site from " + request.getPathInfo());
    }

    try {
      StoreContext storeContext = commerceConnectionInitializer.findConnectionForSite(site)
              .map(CommerceConnection::getStoreContextProvider)
              .flatMap(storeContextProvider -> storeContextProvider.findContextBySite(site))
              .orElse(null);

      if (storeContext == null) {
        // Legal state: A web presence may have sites which are not related to eCommerce.
        LOG.debug("No store context for site {}.", site);
        return;
      }

      // Deep links have a different domain and must thus not be included
      // in sitemaps.org sitemaps.
      boolean deepLinksOnly = useCommerceCategoryLinks(site) && useCommerceProductLinks(site);
      if (deepLinksOnly) {
        LOG.debug("Only deep links for {}", site);
        return;
      }

      request.setAttribute(ABSOLUTE_URI_KEY, absoluteUrls);

      List<Category> categories = storeContext
              .getConnection()
              .getCatalogService()
              .findTopCategories(storeContext.getCatalogAlias(), storeContext);

      generateUrls(categories, site, request, response, protocol, urlCollector);
    } catch (InvalidContextException e) {
      LOG.info("Cannot create a sitemap for '{}' because the site has no valid store context. " +
              "I assume the site is not a shop and proceed without creating a catalog sitemap.", site.getName());
    }
  }

  // --- internal ---------------------------------------------------

  private boolean useCommerceProductLinks(@NonNull Site site) {
    return settingsService
            .getSetting(ProductPageHandler.LIVECONTEXT_POLICY_COMMERCE_PRODUCT_LINKS, Boolean.class, site)
            .orElse(true);
  }

  private boolean useCommerceCategoryLinks(@NonNull Site site) {
    return settingsService
            .getSetting(ExternalNavigationHandler.LIVECONTEXT_POLICY_COMMERCE_CATEGORY_LINKS, Boolean.class, site)
            .orElse(false);
  }

  private void generateUrls(List<Category> categories, @NonNull Site site, @NonNull HttpServletRequest request,
                            @NonNull HttpServletResponse response, String protocol, UrlCollector urlCollector) {
    // Must not include deep links in sitemap
    if (useCommerceProductLinks(site)) {
      return;
    }

    for (Category category : categories) {
      // Only include the category's products if the category has a context,
      // i.e. if some parent is linked into the navigation as an external channel.
      LiveContextNavigation liveContextNavigation = liveContextNavigationFactory.createNavigation(category, site);
      if (liveContextNavigation.getContext() != null) {
        for (Product product : category.getProducts()) {
          ProductInSite productInSite = liveContextNavigationFactory.createProductInSite(product, site);
          generateUrl(productInSite, request, response, protocol, urlCollector);
        }
      }

      generateUrls(category.getChildren(), site, request, response, protocol, urlCollector);
    }
  }

  /**
   * ecommerceItem is a Product or a LiveContextNavigation,
   * which have no common super class.
   */
  private void generateUrl(Object ecommerceItem, @NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response, String protocol, UrlCollector urlCollector) {
    try {
      String link = linkFormatter.formatLink(ecommerceItem, null, request, response, false);

      // Make absolutely absolute
      if (link.startsWith("//")) {
        link = protocol + ":" + link;
      }

      urlCollector.appendUrl(link);
    } catch (Exception e) {
      LOG.warn("cannot create link for " + ecommerceItem, e);
    }
  }
}
