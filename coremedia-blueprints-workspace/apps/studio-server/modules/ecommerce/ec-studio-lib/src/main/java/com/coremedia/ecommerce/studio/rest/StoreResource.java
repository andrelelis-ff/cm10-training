package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.MappedCatalogsProvider;
import com.coremedia.cap.content.Content;
import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.rest.linking.LinkResolver;
import com.coremedia.rest.linking.LinkResolverUtil;
import com.coremedia.rest.linking.ResponseLocationHeaderLinker;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A store {@link com.coremedia.ecommerce.studio.rest.model.Store} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/store/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_WORKSPACE_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class StoreResource extends AbstractCatalogResource<Store> {

  private static final Logger LOG = LoggerFactory.getLogger(StoreResource.class);

  private static final String SHOP_URL_PBE_PARAM = "shopUrl";

  private List<PbeShopUrlTargetResolver> pbeShopUrlTargetResolvers = emptyList();

  @Autowired
  private LinkResolver linkResolver;

  @Autowired
  private CategoryAugmentationHelper categoryAugmentationHelper;

  @Autowired
  private ProductAugmentationHelper productAugmentationHelper;

  @Autowired
  private MappedCatalogsProvider mappedCatalogsProvider;

  @Autowired
  public StoreResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @PostMapping("urlService")
  @Nullable
  public Object handlePost(@PathVariable Map<String, String> params, @RequestBody @NonNull Map<String, Object> rawJson) {
    String shopUrlStr = (String) rawJson.get(SHOP_URL_PBE_PARAM);
    Object resolved = null;
    if (shopUrlStr != null) {
      resolved = findFirstPbeShopUrlTargetResolver(params, shopUrlStr).orElse(null);
    }

    if (resolved == null) {
      LOG.debug("Shop URL '{}' does not resolve to any known entity, returning null.", shopUrlStr);
    } else {
      LOG.debug("Shop URL '{}' resolves to '{}'.", shopUrlStr, resolved);
    }

    return resolved;
  }

  @NonNull
  private Optional<Object> findFirstPbeShopUrlTargetResolver(@NonNull Map<String, String> params,
                                                             @NonNull String shopUrlStr) {
    String siteId = params.get(PATH_SITE_ID);

    return pbeShopUrlTargetResolvers.stream()
            .map(resolver -> resolver.resolveUrl(shopUrlStr, siteId))
            .filter(Objects::nonNull)
            .findFirst();
  }

  @PostMapping("augment")
  @ResponseLocationHeaderLinker
  @Nullable
  public Content augment(@RequestBody @NonNull Map<String, Object> rawJson) {
    Object catalogObject = LinkResolverUtil.resolveJson(rawJson, linkResolver);

    if (catalogObject instanceof Category) {
      return categoryAugmentationHelper.augment((Category) catalogObject);
    } else if (catalogObject instanceof Product) {
      return productAugmentationHelper.augment((Product) catalogObject);
    } else {
      LOG.debug("Cannot augment object {}: only categories are supported. JSON parameters: {}", catalogObject, rawJson);
      return null;
    }
  }

  @Override
  protected StoreRepresentation getRepresentation(@NonNull Map<String, String> params) {
    StoreRepresentation storeRepresentation = new StoreRepresentation();
    fillRepresentation(params, storeRepresentation);
    return storeRepresentation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, @NonNull StoreRepresentation representation) {
    String siteId = params.get(PATH_SITE_ID);
    Store entity = getEntity(params);

    if (entity == null) {
      throw new StoreBeanNotFoundException(HttpStatus.GONE, "Could not load store bean for site with ID '" + siteId + "'.");
    }

    try {
      StoreContext context = entity.getContext();
      List<Catalog> configuredCatalogs = mappedCatalogsProvider.getConfiguredCatalogs(context);
      CommerceConnection connection = context.getConnection();

      representation.setMarketingEnabled(hasMarketingSpots(connection, context));
      representation.setId(entity.getId());
      representation.setVendorUrl(entity.getVendorUrl().orElse(null));
      representation.setVendorName(entity.getVendorName().orElse(null));
      representation.setVendorVersion(entity.getVendorVersion().orElse(null));
      representation.setContext(context);
      representation.setMultiCatalog(configuredCatalogs.size() > 1);
      representation.setDefaultCatalog(entity.getDefaultCatalog().orElse(null));
      representation.setCatalogs(configuredCatalogs);
      representation.setRootCategories(configuredCatalogs.stream()
              .map(Catalog::getRootCategory)
              .collect(toList()));
      representation.setRootCategory(connection.getCatalogService()
              .findRootCategory(context.getCatalogAlias(), context));
      representation.setTimeZoneId(context.getTimeZoneId().map(ZoneId::getId).orElse(null));
    } catch (CommerceException e) {
      LOG.warn("Error loading store bean: {} (site: {})", e.getMessage(), siteId);
      throw e;
    }
  }

  private static boolean hasMarketingSpots(@NonNull CommerceConnection connection, @NonNull StoreContext context) {
    List<MarketingSpot> marketingSpots = connection.getMarketingSpotService()
            .map(marketingSpotService -> marketingSpotService.findMarketingSpots(context))
            .orElseGet(Collections::emptyList);

    return !marketingSpots.isEmpty();
  }

  @Override
  protected Store doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Store::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Store store) {
    StoreContext storeContext = store.getContext();
    Map<String, String> params = new HashMap<>();
    params.put(PATH_SITE_ID, storeContext.getSiteId());
    params.put(PATH_WORKSPACE_ID, storeContext.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }

  @Autowired(required = false)
  void setPbeShopUrlTargetResolvers(List<PbeShopUrlTargetResolver> pbeShopUrlTargetResolvers) {
    this.pbeShopUrlTargetResolvers = pbeShopUrlTargetResolvers;
  }
}
