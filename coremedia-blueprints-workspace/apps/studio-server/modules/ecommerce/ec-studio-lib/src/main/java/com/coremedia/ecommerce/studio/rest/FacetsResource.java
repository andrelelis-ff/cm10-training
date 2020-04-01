package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.ecommerce.studio.rest.model.Facets;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * A facets object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/facets/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_CATALOG_ALIAS + "}/{" + AbstractCatalogResource.PATH_WORKSPACE_ID + "}/{" + AbstractCatalogResource.PATH_ID + ":.+}", produces = MediaType.APPLICATION_JSON_VALUE)
public class FacetsResource extends AbstractCatalogResource<Facets> {

  @Autowired
  public FacetsResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected FacetsRepresentation getRepresentation(@NonNull Map<String, String> params) {
    FacetsRepresentation facetsRepresentation = new FacetsRepresentation();
    fillRepresentation(params, facetsRepresentation);
    return facetsRepresentation;
  }

  protected void fillRepresentation(@NonNull Map<String, String> params, FacetsRepresentation representation) {
    Facets facets = getEntity(params);

    if (facets == null) {
      throw new CatalogBeanNotFoundRestException("Could not load facets bean.");
    }

    representation.setId(facets.getId());

    StoreContext storeContext = getStoreContext(params)
            .orElseThrow(() -> new IllegalArgumentException("store context not available"));
    CommerceConnection commerceConnection = storeContext.getConnection();
    CatalogService catalogService = commerceConnection.getCatalogService();

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId commerceId = commerceConnection.getIdProvider().formatCategoryId(catalogAlias, params.get(PATH_ID));
    Category category = catalogService.findCategoryById(commerceId, storeContext);

    if (category != null) {
      Map<String, List<SearchFacet>> searchFacetMap = catalogService.getFacetsForProductSearch(category, storeContext);
      Map<String, List<Facet>> serializeFacets = simplifySearchFacets(searchFacetMap);
      representation.setFacets(serializeFacets);
    }
  }

  @Override
  protected Facets doGetEntity(@NonNull Map<String, String> params) {
    return getStoreContext(params).map(Facets::new).orElse(null);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Facets facets) {
    StoreContext context = facets.getContext();
    Map<String, String> params = new HashMap<>();
    params.put(PATH_ID, facets.getId());
    params.put(PATH_SITE_ID, context.getSiteId());
    params.put(PATH_CATALOG_ALIAS, context.getCatalogAlias().value());
    params.put(PATH_WORKSPACE_ID, context.getWorkspaceId().orElse(WORKSPACE_ID_NONE).value());
    return params;
  }

  private Map<String, List<Facet>> simplifySearchFacets(Map<String, List<SearchFacet>> searchFacetMap) {
    return searchFacetMap.entrySet().stream().collect(
            toMap(Map.Entry::getKey,
                    entry -> entry.getValue().stream().map(
                            searchFacet -> new Facet(searchFacet.getQuery(), searchFacet.getLabel())
                    ).collect(toList())
            )
    );
  }
}
