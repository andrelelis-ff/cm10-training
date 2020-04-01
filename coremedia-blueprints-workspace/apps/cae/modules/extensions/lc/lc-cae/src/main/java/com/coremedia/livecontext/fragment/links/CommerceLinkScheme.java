package com.coremedia.livecontext.fragment.links;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.contentbeans.LiveContextExternalChannelImpl;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.PreviewUrlService;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.seo.ExternalSeoSegmentBuilder;
import com.coremedia.livecontext.logictypes.CommerceLedLinkBuilderHelper;
import com.coremedia.livecontext.navigation.LiveContextCategoryNavigation;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.link.UrlUtil.convertToQueryParamList;
import static com.coremedia.livecontext.fragment.links.transformers.LiveContextLinkTransformer.LIVECONTEXT_CONTENT_LED;
import static com.coremedia.livecontext.handler.ExternalNavigationHandler.LIVECONTEXT_POLICY_COMMERCE_CATEGORY_LINKS;
import static com.coremedia.livecontext.product.ProductPageHandler.LIVECONTEXT_POLICY_COMMERCE_PRODUCT_LINKS;
import static com.google.common.base.Strings.isNullOrEmpty;

@DefaultAnnotation(NonNull.class)
@Link
public class CommerceLinkScheme {

  // dummy URL meant to be replaced by the commerce link resolver later on
  public static final String DUMMY_URI_STRING = "http://lc-generic-live.vm";
  public static final UriComponents DUMMY_URI_TO_BE_REPLACED = toUriComponents(DUMMY_URI_STRING);

  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final CommerceLedLinkBuilderHelper commerceLedPageExtension;
  private final SettingsService settingsService;
  private final ExternalSeoSegmentBuilder seoSegmentBuilder;

  private boolean preview;

  CommerceLinkScheme(CommerceConnectionSupplier commerceConnectionSupplier,
                     CommerceLedLinkBuilderHelper commerceLedPageExtension,
                     SettingsService settingsService,
                     ExternalSeoSegmentBuilder seoSegmentBuilder) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.commerceLedPageExtension = commerceLedPageExtension;
    this.settingsService = settingsService;
    this.seoSegmentBuilder = seoSegmentBuilder;
  }

  @Link(type = Category.class, order = 1)
  @Nullable
  public UriComponents buildLinkForCategory(@Nullable Category category, Map<String, Object> linkParameters,
                                            HttpServletRequest request) {
    if (category == null) {
      // not responsible
      return null;
    }

    if (!useCommerceCategoryLinks(request)) {
      return null;
    }

    // in fragments the links will actually be build in CommerceLinkResolver (as the last transfomer)
    if (isFragmentRequest(request) && !isContentLed(request)) {
      return DUMMY_URI_TO_BE_REPLACED;
    }

    // from here it is either a studio preview link or we are in a content-led scenario...

    if (isPreview()) {
      // even if it's not a studio preview link (but a link on a content-led page in preview)
      // we currently accept the effort to build the link via PreviewUrlService
      PreviewUrlService previewUrlService = getPreviewUrlService(category);
      if (previewUrlService == null) {
        return null;
      }
      StoreContext storeContext = StoreContextHelper.findStoreContext(request).orElse(category.getContext());
      List<QueryParam> queryParamList = convertToQueryParamList(linkParameters);
      return previewUrlService.getCategoryUrl(category, storeContext, queryParamList, request);
    }

    // a content-led link on the live site is the only remaining case
    return getStorefrontUrl(category.getStorefrontUrl())
            .orElse(DUMMY_URI_TO_BE_REPLACED);
  }

  @Link(type = CategoryInSite.class)
  @Nullable
  public UriComponents buildLinkForCategoryInSite(CategoryInSite categoryInSite, Map<String, Object> linkParameters,
                                                  HttpServletRequest request) {
    return buildLinkForCategory(categoryInSite.getCategory(), linkParameters, request);
  }

  @Link(type = LiveContextCategoryNavigation.class, order = 1)
  @Nullable
  public UriComponents buildLinkForLiveContextCategoryNavigation(LiveContextCategoryNavigation categoryNavigation,
                                                                 Map<String, Object> linkParameters,
                                                                 HttpServletRequest request) {
    return buildLinkForCategory(categoryNavigation.getCategory(), linkParameters, request);
  }

  @Link(type = LiveContextExternalChannelImpl.class, order = 1)
  @Nullable
  public UriComponents buildLinkForExternalChannel(LiveContextExternalChannelImpl augmentedCategory,
                                                   Map<String, Object> linkParameters, HttpServletRequest request) {
    return buildLinkForCategory(augmentedCategory.getCategory(), linkParameters, request);
  }

  @Link(type = Product.class, order = 1)
  @Nullable
  public UriComponents buildLinkForProduct(@Nullable Product product, Map<String, Object> linkParameters,
                                           HttpServletRequest request) {
    if (product == null) {
      // not responsible
      return null;
    }

    if (!useCommerceProductLinks(request)) {
      return null;
    }

    // in fragments the links will actually be build in CommerceLinkResolver (as the last transfomer)
    if (isFragmentRequest(request) && !isContentLed(request)) {
      return DUMMY_URI_TO_BE_REPLACED;
    }

    // from here it is either a studio preview link or we are in a content-led scenario...

    if (isPreview()) {
      // even if it's not a studio preview link (but a link on a content-led page in preview)
      // we currently accept the effort to build the link via PreviewUrlService
      PreviewUrlService previewUrlService = getPreviewUrlService(product);
      if (previewUrlService == null) {
        return null;
      }
      Category category = product.getCategory();
      StoreContext storeContext = StoreContextHelper.findStoreContext(request).orElse(product.getContext());
      List<QueryParam> queryParamList = convertToQueryParamList(linkParameters);
      return previewUrlService.getProductUrl(product, category, storeContext, queryParamList, request);
    }

    // a content-led link on the live site is the only remaining case
    return getStorefrontUrl(product.getStorefrontUrl())
            .orElse(DUMMY_URI_TO_BE_REPLACED);
  }

  @Link(type = ProductInSite.class, order = 1)
  @Nullable
  public UriComponents buildLinkForProductInSite(ProductInSite productInSite, Map<String, Object> linkParameters,
                                                 HttpServletRequest request) {
    return buildLinkForProduct(productInSite.getProduct(), linkParameters, request);
  }

  @Link(type = CMProductTeaser.class, view = HandlerHelper.VIEWNAME_DEFAULT, order = 1)
  @Nullable
  public UriComponents buildLinkForProductTeaser(CMProductTeaser productTeaser, Map<String, Object> linkParameters,
                                                 HttpServletRequest request) {
    return buildLinkForProduct(productTeaser.getProduct(), linkParameters, request);
  }

  @Link(type = LiveContextExternalProduct.class, order = 1)
  @Nullable
  public UriComponents buildLinkForExternalProduct(LiveContextExternalProduct externalProduct,
                                                   Map<String, Object> linkParameters, HttpServletRequest request) {
    return buildLinkForProduct(externalProduct.getProduct(), linkParameters, request);
  }

  @Link(type = CMExternalPage.class, order = 1)
  @Nullable
  public UriComponents buildLinkForExternalPage(CMExternalPage externalPage, Map<String, Object> linkParameters,
                                                HttpServletRequest request) {
    // in fragments the links will actually be build in CommerceLinkResolver (as the last transfomer)
    if (isFragmentRequest(request) && !isContentLed(request)) {
      return DUMMY_URI_TO_BE_REPLACED;
    }

    // from here it is either a studio preview link or we are in a content-led scenario...

    if (isPreview()) {
      // even if it's not a studio preview link (but a link on a content-led page in preview)
      // we currently accept the effort to build the link via PreviewUrlService
      CommerceConnection commerceConnection = findCommerceConnection(externalPage).orElse(null);
      if (commerceConnection == null) {
        return null;
      }
      PreviewUrlService previewUrlService = getPreviewUrlService(commerceConnection);
      if (previewUrlService == null) {
        return null;
      }
      String externalId = externalPage.getExternalId();
      String externalUriPath = externalPage.getExternalUriPath();
      StoreContext storeContext = StoreContextHelper.findStoreContext(request)
              .orElseGet(commerceConnection::getStoreContext);
      List<QueryParam> queryParamList = convertToQueryParamList(linkParameters);

      if (!isNullOrEmpty(externalUriPath)) {
        return previewUrlService.getExternalPageNonSeoUrl(externalUriPath, storeContext, queryParamList, request);
      }

      return previewUrlService.getExternalPageSeoUrl(externalId, storeContext, queryParamList, request);
    }

    // a content-led link on the live site is the only remaining case
    return null;
  }

  @Link(type = CMChannel.class, order = 1)
  @Nullable
  public UriComponents buildLinkForCMChannel(CMChannel channel, Map<String, Object> linkParameters,
                                             HttpServletRequest request) {
    // in fragments the links will actually be build in CommerceLinkResolver (as the last transfomer)
    if (isFragmentRequest(request) && !isContentLed(request)) {
      return DUMMY_URI_TO_BE_REPLACED;
    }

    if (!useCommerceLinkForChannel(channel)) {
      return null;
    }

    // from here it is either a studio preview link or we are in a content-led scenario...

    if (isPreview()) {
      // even if it's not a studio preview link (but a link on a content-led page in preview)
      // we currently accept the effort to build the link via PreviewUrlService
      CommerceConnection commerceConnection = findCommerceConnection(channel).orElse(null);
      if (commerceConnection == null) {
        return null;
      }
      PreviewUrlService previewUrlService = getPreviewUrlService(commerceConnection);
      if (previewUrlService == null) {
        return null;
      }
      String seoPath = seoSegmentBuilder.asSeoSegment(channel, channel);
      StoreContext storeContext = StoreContextHelper.findStoreContext(request)
              .orElseGet(commerceConnection::getStoreContext);
      List<QueryParam> queryParamList = convertToQueryParamList(linkParameters);

      return previewUrlService.getContentUrl(seoPath, storeContext, queryParamList, request);
    }

    // a content-led link on the live site is the only remaining case
    return DUMMY_URI_TO_BE_REPLACED;
  }

  private boolean useCommerceProductLinks(ServletRequest request) {
    return findSiteSetting(request, LIVECONTEXT_POLICY_COMMERCE_PRODUCT_LINKS).orElse(true);
  }

  private boolean useCommerceCategoryLinks(ServletRequest request) {
    return findSiteSetting(request, LIVECONTEXT_POLICY_COMMERCE_CATEGORY_LINKS).orElse(false);
  }

  private Optional<Boolean> findSiteSetting(ServletRequest request, String settingName) {
    return SiteHelper.findSite(request)
            .flatMap(site -> settingsService.getSetting(settingName, Boolean.class, site));
  }

  private boolean useCommerceLinkForChannel(CMChannel channel) {
    return commerceLedPageExtension.isCommerceLedChannel(channel);
  }

  private static boolean isFragmentRequest(@NonNull HttpServletRequest request) {
    return FragmentContextProvider.findFragmentContext(request)
            .map(FragmentContext::isFragmentRequest)
            .orElse(false);
  }

  @Nullable
  private static PreviewUrlService getPreviewUrlService(CommerceConnection commerceConnection) {
    return commerceConnection.getPreviewUrlService().orElse(null);
  }

  @Nullable
  private static PreviewUrlService getPreviewUrlService(CommerceBean commerceBean) {
    return commerceBean.getContext().getConnection().getPreviewUrlService().orElse(null);
  }

  private Optional<CommerceConnection> findCommerceConnection(CMChannel channel) {
    return findCommerceConnection(channel.getContent());
  }

  private Optional<CommerceConnection> findCommerceConnection(Content content) {
    return commerceConnectionSupplier.findConnection(content);
  }

  private static UriComponents toUriComponents(String uri) {
    return UriComponentsBuilder
            .fromUriString(uri)
            .build();
  }

  private static Optional<UriComponents> getStorefrontUrl(@Nullable String storefrontUrl) {
    return Optional.ofNullable(storefrontUrl)
            .filter(s -> !s.isEmpty())
            .map(CommerceLinkScheme::toUriComponents);
  }

  @Value("${cae.is.preview:false}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  public boolean isPreview() {
    return preview;
  }

  private boolean isContentLed(ServletRequest request){
    return findSiteSetting(request, LIVECONTEXT_CONTENT_LED).orElse(false);
  }
}
