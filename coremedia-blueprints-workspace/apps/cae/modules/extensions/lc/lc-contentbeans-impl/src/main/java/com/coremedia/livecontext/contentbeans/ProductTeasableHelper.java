package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.xml.Markup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceId;
import static com.coremedia.xml.MarkupUtil.isEmptyRichtext;

public class ProductTeasableHelper {

  private static final Logger LOG = LoggerFactory.getLogger(ProductTeasableHelper.class);

  private static final String SHOP_NOW_POLICY = "shopNow";
  private static final String SHOP_NOW_POLICY_DISABLED = "disabled";

  private LiveContextNavigationFactory liveContextNavigationFactory;
  private SitesService sitesService;
  private SettingsService settingsService;
  private CommerceConnectionSupplier commerceConnectionSupplier;

  /**
   * Returns the underlying Product in this content's site.
   * <p>
   * You cannot build links for a Product, since a Product can occur in multiple
   * sites and is thus not unique for link building.
   * Use a {@link ProductInSite} for link building.
   *
   * @return a ProductInSite or null if product or site cannot be determined.
   */
  @Nullable
  ProductInSite getProductInSite(@NonNull LiveContextProductTeasable contentBean) {
    Product product = contentBean.getProduct();
    Optional<Site> site = findSite(contentBean);

    if (product == null || !site.isPresent()) {
      return null;
    }

    return liveContextNavigationFactory.createProductInSite(product, site.get().getId());
  }

  boolean isShopNowEnabled(@NonNull LiveContextProductTeasable contentBean, @NonNull CMContext context) {
    Object navigation = context;
    try {
      Product product = contentBean.getProduct();
      Optional<Site> site = findSite(contentBean);
      if (product != null && site.isPresent()) {
        navigation = liveContextNavigationFactory.createNavigation(product.getCategory(), site.get());
      }
    } catch (Exception e) {
      LOG.debug("Unable to get product category for content bean {}.", this, e);
    }

    String shopNow = settingsService.setting(SHOP_NOW_POLICY, String.class, contentBean.getContent(), navigation);
    return !SHOP_NOW_POLICY_DISABLED.equalsIgnoreCase(shopNow);
  }

  public Product getProduct(LiveContextProductTeasable contentBean) {
    Optional<CommerceId> productIdOptional = parseCommerceId(contentBean.getExternalId());

    if (!productIdOptional.isPresent()) {
      return null;
    }

    Optional<CommerceConnection> commerceConnection = commerceConnectionSupplier
            .findConnection(contentBean.getContent());

    if (!commerceConnection.isPresent()) {
      return null;
    }

    CommerceConnection connection = commerceConnection.get();

    StoreContext storeContext = connection.getStoreContext();
    CommerceId commerceId = productIdOptional.get();

    try {
      return connection.getCatalogService().findProductById(commerceId, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve product '{}' in store '{}'.", commerceId, storeContext.getStoreName(), e);
      return null;
    }
  }

  Markup getTeaserTextInternal(@NonNull LiveContextProductTeasable contentBean, Markup teaserTextFromContent) {
    Markup tt = teaserTextFromContent;

    //fetch the product short description for the teaser text in case of empty teaser text
    if (isEmptyRichtext(tt, true)) {
      try {
        Product product = contentBean.getProduct();
        if (product != null) {
          tt = product.getShortDescription();
        }
      } catch (CommerceException e) {
        LOG.debug("Could not load product with id '{}'.", contentBean.getExternalId(), e);
      }
    }

    return tt;
  }

  String getTeaserTitleInternal(@NonNull LiveContextProductTeasable contentBean, String teaserTitleFromContent) {
    String tt = teaserTitleFromContent;

    //fetch the product name for the teaser title in case of empty teaser title
    if (isNullOrBlank(tt)) {
      try {
        Product product = contentBean.getProduct();
        if (product != null) {
          tt = product.getName();
        }
      } catch (CommerceException e) {
        LOG.debug("Could not load product with id '{}'.", contentBean.getExternalId(), e);
      }
    }

    return tt;
  }

  @NonNull
  private Optional<Site> findSite(@NonNull LiveContextProductTeasable contentBean) {
    return sitesService.getContentSiteAspect(contentBean.getContent()).findSite();
  }

  static boolean isNullOrBlank(@Nullable String s) {
    return s == null || s.trim().isEmpty();
  }

  @Autowired
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Autowired
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setCommerceConnectionSupplier(CommerceConnectionSupplier commerceConnectionSupplier) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }
}
