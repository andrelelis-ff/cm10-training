package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.links.PostProcessorPrecendences;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.contentbeans.LiveContextExternalChannelImpl;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextCategoryNavigation;
import com.coremedia.livecontext.product.ProductList;
import com.coremedia.livecontext.product.ProductListSubstitutionService;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LinkPostProcessor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
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
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.ContentTypes.CONTENT_TYPE_HTML;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_REST;
import static com.coremedia.blueprint.cae.constants.RequestAttributeConstants.setPage;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static org.springframework.util.Assert.hasText;

@Link
@RequestMapping
@LinkPostProcessor
public class ExternalNavigationHandler extends LiveContextPageHandlerBase {

  public static final String LIVECONTEXT_POLICY_COMMERCE_CATEGORY_LINKS = "livecontext.policy.commerce-category-links";

  public static final String REQUEST_ATTRIBUTE_CATEGORY = "livecontext.category";
  private static final String SEGMENT_CATEGORY = "category";
  private static final String SITE_CHANNEL_ID = "siteChannelID";
  private static final String CATEGORY_PATH_VARIABLE = "categoryPath";
  private static final String CATEGORY_SEO_SEGMENT = "categorySeoSegment";
  private static final String PARAM_START = "start";
  private static final String PARAM_STEPS = "steps";
  private static final String PAGING_VIEW = "productPaging";
  private static final String DEFAULT_STEPS = "" + ProductListSubstitutionService.DEFAULT_STEPS;

  private ProductListSubstitutionService productListSubstitutionService;
  private TreeRelation<Content> treeRelation;

  // e.g. /category/shopName/and/here/comes/a/category/path
  public static final String URI_PATTERN
          = "/" + SEGMENT_CATEGORY
          + "/{" + SHOP_NAME_VARIABLE + "}"
          + "/{" + CATEGORY_PATH_VARIABLE + ":" + PATTERN_SEGMENTS + "}";

  public static final String REST_URI_PATTERN
          = '/' + PREFIX_SERVICE
          + '/' + SEGMENT_REST
          + "/{" + SITE_CHANNEL_ID + "}"
          + "/" + SEGMENT_CATEGORY
          + "/{" + CATEGORY_SEO_SEGMENT + "}";

  @GetMapping(URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SHOP_NAME_VARIABLE) String shopSegment,
                                    @PathVariable(CATEGORY_PATH_VARIABLE) String segment,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) String view,
                                    @NonNull HttpServletRequest request) {
    // This handler is only responsible for CAE category links.
    // If the application runs in wcsCategoryLinks mode, we render native
    // WCS links, and this kind of link cannot occur.
    Site site = getSiteResolver().findSiteBySegment(shopSegment);
    if (site != null && useCommerceCategoryLinks(site)) {
      return HandlerHelper.notFound("Unsupported link format");
    }

    hasText(shopSegment, "No shop name provided.");
    hasText(segment, "No segment provided.");

    return createLiveContextPage(shopSegment, segment, view, UserVariantHelper.getUser(request));
  }

  @GetMapping(value = REST_URI_PATTERN, produces = CONTENT_TYPE_HTML)
  @ResponseBody
  public ModelAndView getProducts(
          @PathVariable(SITE_CHANNEL_ID) CMNavigation context,
          @PathVariable(CATEGORY_SEO_SEGMENT) String categorySeoSegment,
          @RequestParam(value = PARAM_START, required = false, defaultValue = "0") Integer start,
          @RequestParam(value = PARAM_STEPS, required = false, defaultValue = DEFAULT_STEPS) Integer steps,
          @NonNull HttpServletRequest request
  ) {
    LiveContextNavigation navigation = getLiveContextNavigationFactory()
            .createNavigationBySeoSegment(context.getContent(), categorySeoSegment);

    ProductList productList = productListSubstitutionService.getProductList(navigation, start, steps);
    Page page = asPage(context, context, treeRelation, UserVariantHelper.getUser(request));

    ModelAndView modelAndView = HandlerHelper.createModelWithView(productList, PAGING_VIEW);
    setPage(modelAndView, page);

    //we need to apply the navigation here, otherwise the template lookup can't decide which context to use
    NavigationLinkSupport.setNavigation(modelAndView, page.getNavigation().getRootNavigation());
    return modelAndView;
  }

  @Nullable
  @Link(type = LiveContextExternalChannelImpl.class)
  public UriComponents buildLinkForExternalChannel(LiveContextExternalChannelImpl navigation, String viewName,
                                                   Map<String, Object> linkParameters,
                                                   @NonNull HttpServletRequest request) {
    // only responsible in non-preview mode
    if (isPreview()) {
      return null;
    }

    return buildCatalogLink(navigation, viewName, linkParameters, request).orElse(null);
  }

  @Nullable
  @Link(type = LiveContextCategoryNavigation.class)
  public UriComponents buildLinkForCategoryImpl(LiveContextCategoryNavigation navigation, String viewName,
                                                Map<String, Object> linkParameters,
                                                @NonNull HttpServletRequest request) {
    return buildCatalogLink(navigation, viewName, linkParameters, request).orElse(null);
  }

  @Nullable
  @Link(type = CategoryInSite.class)
  public UriComponents buildLinkFor(CategoryInSite categoryInSite, String viewName, Map<String, Object> linkParameters,
                                    @NonNull HttpServletRequest request) {
    LiveContextNavigation navigation = getLiveContextNavigationFactory()
            .createNavigation(categoryInSite.getCategory(), categoryInSite.getSite());

    return buildCatalogLink(navigation, viewName, linkParameters, request).orElse(null);
  }

  @LinkPostProcessor(type = LiveContextExternalChannelImpl.class, order = PostProcessorPrecendences.MAKE_ABSOLUTE)
  public UriComponents makeAbsoluteUri(UriComponents originalUri, LiveContextExternalChannelImpl liveContextNavigation,
                                       Map<String, Object> linkParameters, @NonNull HttpServletRequest request) {
    return doMakeAbsoluteUri(originalUri, liveContextNavigation, linkParameters, request);
  }

  // --------------------  Helper ---------------------------

  public boolean useCommerceCategoryLinks(@NonNull Site site) {
    return getSettingsService()
            .getSetting(LIVECONTEXT_POLICY_COMMERCE_CATEGORY_LINKS, Boolean.class, site)
            .orElse(false);
  }

  private UriComponents doMakeAbsoluteUri(UriComponents originalUri,
                                          @NonNull LiveContextNavigation liveContextNavigation,
                                          Map<String, Object> linkParameters, @NonNull HttpServletRequest request) {
    Site site = liveContextNavigation.getSite();

    // Native category links are absolute anyway, nothing more to do here.
    if (useCommerceCategoryLinks(site)) {
      return originalUri;
    }

    return absoluteUri(originalUri, liveContextNavigation, site, linkParameters, request);
  }

  @NonNull
  private Optional<UriComponents> buildCatalogLink(@NonNull LiveContextNavigation navigation, String viewName,
                                                   Map<String, Object> linkParameters,
                                                   @NonNull HttpServletRequest request) {
    Category category = findCategory(navigation).orElse(null);

    if (category == null) {
      return Optional.empty();
    }

    Site site = navigation.getSite();

    // if it's commerce led the commerce link scheme is responsible to build the link
    if (!useCommerceCategoryLinks(site)) {
      return buildCaeLinkForCategory(navigation, viewName, linkParameters);
    }

    return Optional.empty();
  }

  @NonNull
  public static Optional<Category> findCategory(@NonNull LiveContextNavigation navigation) {
    try {
      return Optional.ofNullable(navigation.getCategory());
    } catch (NotFoundException e) {
      LOG.debug("ignoring commerce exception", e);
      return Optional.empty();
    }
  }

  @NonNull
  private ModelAndView createLiveContextPage(@NonNull String shopSegment, @NonNull String segment, String view,
                                             @Nullable User developer) {
    Site site = getSiteResolver().findSiteBySegment(shopSegment);

    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection commerceConnection = storeContext.getConnection();

    Category category = commerceConnection.getCatalogService()
            .findCategoryBySeoSegment(segment, commerceConnection.getStoreContext());

    Navigation context = getNavigationContext(site, category).orElse(null);
    if (context == null) {
      LOG.warn("Cannot find category for seo segment '{}'", segment);
      return HandlerHelper.notFound("No such category");
    }

    Page page = asPage(context, context, treeRelation, developer);

    ModelAndView modelAndView = createModelAndView(page, view);
    modelAndView.addObject(REQUEST_ATTRIBUTE_CATEGORY, context);
    return modelAndView;
  }

  @NonNull
  public Optional<UriComponents> buildCaeLinkForCategory(@NonNull LiveContextNavigation navigation, String viewName,
                                                         Map<String, Object> linkParameters) {
    // If there is no root navigation for the given category, it must be a category that is not reachable
    // via the (content based) navigation. This is not an invalid state. There might be another
    // link scheme that is able to produce links to categories, which are not part of the navigation. Hence
    // this link scheme returns null, so that the link formatter may choose a different link scheme.
    Site site = navigation.getSite();
    String siteSegment = getSiteSegment(site);

    Category category = findCategory(navigation).orElse(null);
    if (category == null) {
      return Optional.empty();
    }

    String navigationSegment = category.getSeoSegment();
    if (!StringUtils.hasText(navigationSegment)) {
      LOG.warn("Unable to build link for category {} because it has no seosegment", category);
      return Optional.empty();
    }

    UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .newInstance()
            .pathSegment(SEGMENT_CATEGORY)
            .pathSegment(siteSegment)
            .pathSegment(navigationSegment);

    addViewAndParameters(uriBuilder, viewName, linkParameters);

    UriComponents uri = uriBuilder.build();
    return Optional.of(uri);
  }

  // --------------- Config -------------------------

  @Required
  public void setProductListSubstitutionService(ProductListSubstitutionService productListSubstitutionService) {
    this.productListSubstitutionService = productListSubstitutionService;
  }

  public void setTreeRelation(TreeRelation<Content> treeRelation) {
    this.treeRelation = treeRelation;
  }
}
