package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A catalog {@link Catalog} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "livecontext/catalog/{" + AbstractCatalogResource.PATH_SITE_ID + "}/{" + AbstractCatalogResource.PATH_ID + "}", produces = MediaType.APPLICATION_JSON_VALUE)
public class CatalogResource extends AbstractCatalogResource<Catalog> {

  @Autowired
  public CatalogResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @Override
  protected CatalogRepresentation getRepresentation(@NonNull Map<String, String> params) {
    CatalogRepresentation representation = new CatalogRepresentation();
    fillRepresentation(params, representation);
    return representation;
  }

  private void fillRepresentation(@NonNull Map<String, String> params, CatalogRepresentation representation) {
    Catalog entity = getEntity(params);

    if (entity == null) {
      LOG.warn("Error loading catalog bean");
      throw new CatalogRestException(HttpStatus.NOT_FOUND, CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, "Could not load workspace bean");
    }

    representation.setDefault(entity.isDefaultCatalog());
    representation.setId(CommerceIdFormatterHelper.format(entity.getId()));
    representation.setName(entity.getName().value());

    Category rootCategory = entity.getRootCategory();
    representation.setTopCategories(rootCategory.getChildren());
    representation.setRootCategory(rootCategory);
  }

  @Override
  protected Catalog doGetEntity(@NonNull Map<String, String> params) {
    CatalogId catalogId = getCatalogId(params);

    StoreContext storeContext = getStoreContext(params).orElse(null);
    if (storeContext == null) {
      return null;
    }

    return storeContext.getConnection()
            .getCatalogService()
            .getCatalog(catalogId, storeContext)
            .orElse(null);
  }

  @NonNull
  @Override
  protected Optional<StoreContext> getStoreContext(@NonNull Map<String, String> params) {
    return super.getStoreContext(params).map(storeContext -> enhanceWithCatalogAlias(storeContext, params));
  }

  @NonNull
  private StoreContext enhanceWithCatalogAlias(@NonNull StoreContext storeContext, @NonNull Map<String, String> params) {
    StoreContextProvider storeContextProvider = storeContext.getConnection().getStoreContextProvider();
    StoreContextBuilder clonedContextBuilder = storeContextProvider.buildContext(storeContext);

    String siteId = params.get(PATH_SITE_ID);
    CatalogId catalogId = getCatalogId(params);
    CatalogAlias catalogAlias = CatalogAlias.ofNullable(params.get(PATH_CATALOG_ALIAS)).orElse(null);

    CatalogAliasTranslationService catalogAliasTranslationService = getCatalogAliasTranslationService();
    if (catalogAlias != null) {
      clonedContextBuilder = clonedContextBuilder
              .withCatalogId(catalogId)
              .withCatalogAlias(catalogAlias);
    } else {
      Optional<CatalogAlias> catalogAliasForId = catalogAliasTranslationService
              .getCatalogAliasForId(catalogId, storeContext);

      clonedContextBuilder = clonedContextBuilder
              .withCatalogId(catalogId)
              .withCatalogAlias(catalogAliasForId.orElse(null));
    }

    return clonedContextBuilder.build();
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Catalog catalog) {
    CommerceId commerceId = catalog.getId();
    String extId = commerceId.getExternalId().orElseGet(catalog::getExternalId);
    Map<String, String> params = new HashMap<>();
    params.put(PATH_ID, extId);
    params.put(PATH_SITE_ID, catalog.getContext().getSiteId());
    return params;
  }

  private CatalogId getCatalogId(@NonNull Map<String, String> params) {
    return CatalogId.of(params.get(PATH_ID));
  }
}
