package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceIdProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpotService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * A catalog {@link com.coremedia.livecontext.ecommerce.p13n.MarketingSpot} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/marketingspot/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_WORKSPACE_ID + "}/{" + AbstractCatalogResource.PATH_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class MarketingSpotResource extends AbstractCatalogResource<MarketingSpot> {


  @Autowired
  public MarketingSpotResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected MarketingSpotRepresentation getRepresentation(@NonNull Map<String, String> params) {
    MarketingSpotRepresentation representation = new MarketingSpotRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, MarketingSpotRepresentation representation) {
    MarketingSpot entity = getEntity(params);

    if (entity == null) {
      throw new CatalogBeanNotFoundRestException("Could not load spot bean");
    }

    representation.setId(CommerceIdFormatterHelper.format(entity.getId()));
    representation.setName(entity.getName());
    representation.setShortDescription(entity.getDescription());
    representation.setExternalId(entity.getExternalId());
    representation.setExternalTechId(entity.getExternalTechId());
    representation.setStore(new Store(entity.getContext()));
  }

  @Override
  protected MarketingSpot doGetEntity(@NonNull Map<String, String> params) {
    StoreContext storeContext = getStoreContext(params).orElse(null);
    if (storeContext == null) {
      return null;
    }

    CommerceConnection connection = storeContext.getConnection();
    MarketingSpotService marketingSpotService = connection.getMarketingSpotService().orElse(null);
    if (marketingSpotService == null) {
      return null;
    }

    CommerceIdProvider idProvider = connection.getIdProvider();
    if (!(idProvider instanceof BaseCommerceIdProvider)) {
      return null;
    }

    CommerceId commerceId = ((BaseCommerceIdProvider) idProvider)
            .builder(BaseCommerceBeanType.MARKETING_SPOT)
            .withExternalId(params.get(PATH_ID))
            .build();

    return marketingSpotService.findMarketingSpotById(commerceId, storeContext);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull MarketingSpot spot) {
    Map<String, String> params = new HashMap<>();
    CommerceId commerceId = spot.getId();
    String extId = commerceId.getExternalId()
            .orElse(spot.getExternalId());
    params.put(PATH_ID, extId);

    StoreContext storeContext = spot.getContext();

    params.put(PATH_SITE_ID, storeContext.getSiteId());
    params.put(PATH_WORKSPACE_ID, storeContext.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
