package com.coremedia.livecontext.preview;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.handler.ExternalNavigationHandler;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.livecontext.navigation.ProductInSiteImpl;
import com.coremedia.livecontext.product.ProductPageHandler;
import com.coremedia.objectserver.web.links.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.cap.multisite.SiteHelper.getSiteFromRequest;

@Named
@Link
public class CommerceBeanPreviewLinkScheme {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommerceBeanPreviewLinkScheme.class);

  @Inject
  private ExternalNavigationHandler externalNavigationHandler;
  @Inject
  private ProductPageHandler productPageHandler;
  @Inject
  private LiveContextNavigationFactory liveContextNavigationFactory;

  @Link(type = CommerceBean.class, order = 0)
  public Object buildLinkForStudioPreview(CommerceBean commerceBean, String viewName,
                                          Map<String, Object> linkParameters, HttpServletRequest request) {
    Site site = getSiteFromRequest(request);
    if (null == site) {
      LOGGER.debug("no site given, cannot build preview link for commerce bean {}", commerceBean);
      return null;
    }

    try {
      return buildPreviewLink(commerceBean, viewName, linkParameters, request, site);
    } catch (CommerceException e) {
      LOGGER.debug("cannot build preview link for commerce bean {}, ignoring exception", commerceBean, e);
    }

    return null;
  }

  private Object buildPreviewLink(CommerceBean commerceBean, String viewName, Map<String, Object> linkParameters, HttpServletRequest request, Site site) {
    if (commerceBean instanceof Product) {
      ProductInSite productInSite = new ProductInSiteImpl((Product) commerceBean, site);

      return productPageHandler.buildLinkFor(productInSite, viewName, linkParameters, request);
    } else if (commerceBean instanceof Category) {
      CategoryInSite categoryInSite = liveContextNavigationFactory.createCategoryInSite((Category) commerceBean, site);

      return externalNavigationHandler.buildLinkFor(categoryInSite, viewName, linkParameters, request);
    }

    LOGGER.debug("cannot build preview link for commerce bean {} (only product and category are supported)", commerceBean);
    return null;
  }

}
