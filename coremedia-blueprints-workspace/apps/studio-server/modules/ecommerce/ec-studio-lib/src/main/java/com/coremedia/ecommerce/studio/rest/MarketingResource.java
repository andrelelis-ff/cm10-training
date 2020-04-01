package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Marketing;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * The resource is just used for handling the top level store node "Marketing Spots".
 * It is not necessary for the commerce API. This ensures a unified handling
 * of tree nodes in the Studio library window.
 */
@RestController
@RequestMapping(value = "livecontext/marketing/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_WORKSPACE_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class MarketingResource extends AbstractCatalogResource<Marketing> {

  @Autowired
  public MarketingResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected MarketingRepresentation getRepresentation(@NonNull Map<String, String> params) {
    MarketingRepresentation representation = new MarketingRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, MarketingRepresentation representation) {
    Marketing entity = getEntity(params);

    if (entity == null) {
      throw new CatalogBeanNotFoundRestException("Could not load marketing bean.");
    }

    representation.setId(entity.getId());

    StoreContext storeContext = entity.getContext();
    storeContext.getConnection()
            .getMarketingSpotService()
            .map(marketingSpotService -> marketingSpotService.findMarketingSpots(storeContext))
            .ifPresent(representation::setMarketingSpots);
  }

  @Override
  protected Marketing doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Marketing::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Marketing marketing) {
    StoreContext storeContext = marketing.getContext();
    Map<String, String> params = new HashMap<>();
    params.put(PATH_SITE_ID, storeContext.getSiteId());
    params.put(PATH_WORKSPACE_ID, storeContext.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }
}
