package com.coremedia.livecontext.elastic.social.cae;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.common.contentbeans.CMPlaceholder;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.elastic.social.cae.controller.AbstractReviewsResultHandler;
import com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceIdOrThrow;

@RequestMapping
@Link
public class ProductReviewsResultHandler extends AbstractReviewsResultHandler {

  public static final String PLACEHOLDER_ID = "es-reviews-placeholder";
  private static final String PRODUCT_REVIEWS_PREFIX = "product-reviews";
  private static final String PRODUCT_ID = "productId";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/product-reviews/{segment}/{contextId}?productId={productId}"
   */
  public static final String DYNAMIC_PATTERN_PRODUCT_REVIEWS
          = "/" + PREFIX_DYNAMIC
          + "/" + SEGMENTS_FRAGMENT
          + "/" + PRODUCT_REVIEWS_PREFIX
          + "/{" + ROOT_SEGMENT + "}"
          + "/{" + CONTEXT_ID + "}";

  @Inject
  @Named("resolveLivecontextContextStrategy")
  private ResolveContextStrategy contextStrategy;

  private CatalogAliasTranslationService catalogAliasTranslationService;

  @GetMapping(value = DYNAMIC_PATTERN_PRODUCT_REVIEWS)
  public ModelAndView getReviews(@PathVariable(CONTEXT_ID) String contextId,
                                 @RequestParam(value = PRODUCT_ID, required = true) String productId,
                                 @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                 HttpServletRequest request) {
    return handleGetReviews(SiteHelper.getSiteFromRequest(request), contextId, productId, view);
  }

  @PostMapping(value = DYNAMIC_PATTERN_PRODUCT_REVIEWS)
  public ModelAndView createReview(@PathVariable(CONTEXT_ID) String contextId,
                                   @RequestParam(value = PRODUCT_ID, required = true) String productId,
                                   @RequestParam(value = "text", required = false) String text,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "rating", required = false) Integer rating,
                                   HttpServletRequest request) {
    return handleCreateReview(contextId, productId, text, title, rating, request);
  }

  // ---------------------- building links ---------------------------------------------------------------------

  @Link(type = ProductReviewsResult.class, uri = DYNAMIC_PATTERN_PRODUCT_REVIEWS)
  public UriComponents buildLink(ReviewsResult reviewsResult, UriTemplate uriTemplate,
                                 Map<String, Object> linkParameters, HttpServletRequest request) {
    Product product = (Product) reviewsResult.getTarget();
    linkParameters.put(PRODUCT_ID, CommerceIdFormatterHelper.format(product.getReference()));
    return buildFragmentUri(SiteHelper.getSiteFromRequest(request), reviewsResult, uriTemplate, linkParameters);
  }

  // ---------------------- private helper methods ---------------------------------------------------------------------

  @Override
  protected UriComponentsBuilder getUriComponentsBuilder(Site site, ReviewsResult result, UriTemplate uriTemplate) {
    Product product = (Product) result.getTarget();
    Navigation navigation = getContextHelper().currentSiteContext();

    if (site != null && contextStrategy != null) {
      navigation = contextStrategy.resolveContext(site, product).orElse(null);
    }

    return getUriComponentsBuilder(uriTemplate, navigation, product.getId());
  }

  @Substitution(PLACEHOLDER_ID)
  @SuppressWarnings("unused")
  public ProductReviewsResult getReviews(@Nullable CMPlaceholder placeholder, @NonNull HttpServletRequest request) {
    ProductReviewsResult result = null;

    if (placeholder != null) {
      FragmentParameters params = FragmentContextProvider.getFragmentContext(request).getParameters();
      if (params != null) {
        String productId = params.getProductId();
        Optional<CatalogId> catalogId = params.getCatalogId();
        if (StringUtils.isNotBlank(productId)) {
          Site site = SiteHelper.getSiteFromRequest(request);
          if (site != null) {
            Product product = getProduct(productId, site, catalogId.orElse(null));
            if (product == null) {
              LOG.warn("Product with ID '{}' for Site '{}' could not be resolved", productId, site);
              throw new NotFoundException(
                      "Product with ID " + productId + " for Site with ID " + site.getId() + " could not be resolved");
            }

            result = getReviewsResult(product);
          }
        }
      }
    }

    if (result == null) {
      LOG.info("unable to find product reviews for page {} and request {}", placeholder, request.getRequestURI());
    }

    return result;
  }

  @Override
  protected Object getContributionTarget(String productId, Site site) {
    CommerceId commerceId = parseCommerceIdOrThrow(productId);

    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    Product product = connection.getCatalogService().findProductById(commerceId, storeContext);
    if (product == null) {
      LOG.warn("Product with ID '{}' for Site '{}' could not be resolved", productId, site);
      throw new NotFoundException(
              "Product with ID " + productId + " for Site with ID " + site.getId() + " + could not be resolved");
    }

    return product;
  }

  private Product getProduct(@NonNull String productTechId, @NonNull Site site, @Nullable CatalogId catalogId) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    CatalogAlias catalogAlias = Optional.ofNullable(catalogId)
            .flatMap(id -> catalogAliasTranslationService.getCatalogAliasForId(id, storeContext))
            .orElse(DEFAULT_CATALOG_ALIAS);

    CommerceId techId = connection.getIdProvider().formatProductTechId(catalogAlias, productTechId);
    Product product = connection.getCatalogService().findProductById(techId, storeContext);

    if (product instanceof ProductVariant) {
      // we only use products as targets for reviews, no product variants (SKUs)
      // e.g. only store the review for PC_TSHIRT and not for PC_TSHIRT_BLUE_XXL
      product = ((ProductVariant) product).getParent();

      if (product != null && LOG.isDebugEnabled()) {
        LOG.debug("productId {} is a ProductVariant using parent product {} instead", productTechId, product);
      }
    }

    return product;
  }

  private ProductReviewsResult getReviewsResult(Object target) {
    return new ProductReviewsResult(target);
  }

  @Override
  protected ProductReviewsResult getReviewsResult(Object target, boolean feedbackEnabled,
                                                  ContributionType contributionType,
                                                  ElasticSocialConfiguration elasticSocialConfiguration) {
    CommunityUser user = getElasticSocialUserHelper().getCurrentUser();
    return new ProductReviewsResult(target, user, getElasticSocialService(), feedbackEnabled, contributionType,
            elasticSocialConfiguration);
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }
}
