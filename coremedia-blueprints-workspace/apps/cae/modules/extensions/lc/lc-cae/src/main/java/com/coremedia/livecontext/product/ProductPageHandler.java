package com.coremedia.livecontext.product;

import com.coremedia.blueprint.base.links.PostProcessorPrecendences;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.contentbeans.ProductDetailPage;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LinkPostProcessor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.ContentTypes.CONTENT_TYPE_HTML;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_REST;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

@Link
@RequestMapping
@LinkPostProcessor
public class ProductPageHandler extends LiveContextPageHandlerBase {

  public static final String LIVECONTEXT_POLICY_COMMERCE_PRODUCT_LINKS = "livecontext.policy.commerce-product-links";

  private static final String SEGMENT_PRODUCT = "product";
  private static final String PRODUCT_PATH_VARIABLE = "productPath";
  private static final String SITE_CHANNEL_ID = "siteChannelID";
  private static final String PRODUCT_SEO_SEGMENT = "productSeoSegment";
  private static final String PRODUCT_QUICKINFO_SEGMENT = "productQuickinfo";
  private static final String PDP_PAGE_ID = "pdpPage";

  public static final String URI_PATTERN
          = "/" + SEGMENT_PRODUCT
          + "/{" + SHOP_NAME_VARIABLE + "}"
          + "/{" + PRODUCT_PATH_VARIABLE + ":" + PATTERN_SEGMENTS + "}";

  public static final String REST_URI_PATTERN
          = '/' + PREFIX_SERVICE
          + '/' + SEGMENT_REST
          + "/{" + SITE_CHANNEL_ID
          + "}/" + PRODUCT_QUICKINFO_SEGMENT
          + "/{" + PRODUCT_SEO_SEGMENT + "}";

  private boolean useContentPagegrid = false;

  /**
   * Determines whether to use the page grid for content pages or for
   * product detail pages.
   * <p>
   * Default is false, which means the pdp pagegrid is used.
   */
  public void setUseContentPagegrid(boolean useContentPagegrid) {
    this.useContentPagegrid = useContentPagegrid;
  }

  // --- Handler ----------------------------------------------------

  @GetMapping({URI_PATTERN})
  public ModelAndView handleRequest(@PathVariable(SHOP_NAME_VARIABLE) String shopSegment,
                                    @PathVariable(PRODUCT_PATH_VARIABLE) String seoSegment,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) String view,
                                    HttpServletRequest request) {
    // This handler is only responsible for CAE product links.
    // If the application runs in wcsProductLinks mode, we render native
    // WCS links, and this kind of link cannot occur.
    // multi catalog support only for commerce-led implemented by now.

    Site site = getSiteResolver().findSiteBySegment(shopSegment);

    if (useCommerceProductLinks(site)) {
      return HandlerHelper.notFound("Unsupported link format");
    }

    if (StringUtils.isEmpty(seoSegment)) {
      return HandlerHelper.notFound("No product path found");
    }

    return createLiveContextPage(site, seoSegment, view, UserVariantHelper.getUser(request));
  }

  @GetMapping(value = REST_URI_PATTERN, produces = CONTENT_TYPE_HTML)
  @ResponseBody
  public ModelAndView getProducts(@PathVariable(SITE_CHANNEL_ID) CMNavigation context,
                                  @PathVariable(PRODUCT_SEO_SEGMENT) String productId,
                                  HttpServletRequest request) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    Product product = connection.getCatalogService()
            .findProductBySeoSegment(productId, storeContext);

    Site site = getSitesService().getContentSiteAspect(context.getContent()).findSite()
            .orElseThrow(() -> new IllegalArgumentException("Site for context does not exist"));

    ProductInSite productInSite = getLiveContextNavigationFactory().createProductInSite(product, site.getId());
    ModelAndView modelAndView = HandlerHelper.createModel(productInSite);

    Page page = asPage(context, context, UserVariantHelper.getUser(request));
    modelAndView.addObject("cmpage", page);
    //we need to apply the navigation here, otherwise the template lookup can't decide which context to use
    NavigationLinkSupport.setNavigation(modelAndView, page.getNavigation().getRootNavigation());

    return modelAndView;
  }

  // --- Linkscheme -------------------------------------------------

  /**
   * In default mode (wcsProductLinks==true) buildLinkFor builds native
   * WCS links which have no handler counterpart in the CAE.
   * In !wcsProductLinks mode buildLinkFor builds CAE links
   * which are handled by {@link #handleRequest(String, String, String, HttpServletRequest)}.
   */
  @Nullable
  @Link(type = ProductInSite.class)
  public UriComponents buildLinkFor(ProductInSite productInSite, String viewName, Map<String, Object> linkParameters,
                                    HttpServletRequest request) {
    Site site = productInSite.getSite();

    // build link only for content led, the commerce led case is handled by the commerce link scheme
    if (!useCommerceProductLinks(site)) {
      return buildCaeLinkFor(productInSite, viewName, linkParameters);
    }

    return null;
  }

  /**
   * This link is built when the product teaser is inside a rich text.
   * We use the ProductInPage link building logic here.
   */
  @Nullable
  @Link(type = CMProductTeaser.class, view = HandlerHelper.VIEWNAME_DEFAULT)
  public UriComponents buildLinkFor(CMProductTeaser productTeaser, String viewName, Map<String, Object> linkParameters,
                                    HttpServletRequest request) {
    ProductInSite productInSite = productTeaser.getProductInSite();
    if (productInSite == null) {
      return null;
    }

    return buildLinkFor(productInSite, viewName, linkParameters, request);
  }

  @LinkPostProcessor(type = ProductInSite.class, order = PostProcessorPrecendences.MAKE_ABSOLUTE)
  public UriComponents makeAbsoluteUri(UriComponents originalUri, ProductInSite product,
                                       Map<String, Object> linkParameters, HttpServletRequest request) {
    // Native product links are absolute anyway, nothing more to do here.
    Site site = product.getSite();
    return useCommerceProductLinks(site)
            ? originalUri
            : absoluteUri(originalUri, product, product.getSite(), linkParameters, request);
  }

  @NonNull
  @Override
  protected PageImpl createPageImpl(Object content, Navigation context, @Nullable User developer) {
    return useContentPagegrid
            ? super.createPageImpl(content, context, developer)
            : createProductDetailPage(content, context, developer);
  }

  @NonNull
  private ProductDetailPage createProductDetailPage(Object content, Navigation context, User developer) {
    ProductDetailPage page = getBeanFactory().getBean(PDP_PAGE_ID, ProductDetailPage.class);
    page.setContent(content);
    page.setNavigation(context);
    page.setDeveloper(developer);
    return page;
  }

  // --- internal ---------------------------------------------------

  private boolean useCommerceProductLinks(Site site) {
    return getSettingsService()
            .settingWithDefault(LIVECONTEXT_POLICY_COMMERCE_PRODUCT_LINKS, Boolean.class, true, site);
  }

  private ModelAndView createLiveContextPage(@NonNull Site site, @NonNull String seoSegment, String view,
                                             @Nullable User developer) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    Product product = connection.getCatalogService()
            .findProductBySeoSegment(seoSegment, storeContext);
    if (product == null) {
      throw new NullPointerException("No product found for SEO segment '" + seoSegment + "'.");
    }

    Navigation context = getNavigationContext(site, product).orElse(null);
    ProductInSite productInSite = getLiveContextNavigationFactory().createProductInSite(product, site.getId());

    PageImpl page = createPageImpl(productInSite, context, developer);
    page.setTitle(product.getTitle());
    page.setDescription(product.getTitle());
    page.setKeywords(product.getMetaKeywords());

    return createModelAndView(page, view);
  }

  @NonNull
  private UriComponents buildCaeLinkFor(ProductInSite productInSite, String viewName,
                                        Map<String, Object> linkParameters) {
    String siteSegment = getSiteSegment(productInSite.getSite());
    String productSegment = productInSite.getProduct().getSeoSegment();

    UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .newInstance()
            .pathSegment(SEGMENT_PRODUCT)
            .pathSegment(siteSegment)
            .pathSegment(productSegment);

    addViewAndParameters(uriBuilder, viewName, linkParameters);

    return uriBuilder.build();
  }
}
