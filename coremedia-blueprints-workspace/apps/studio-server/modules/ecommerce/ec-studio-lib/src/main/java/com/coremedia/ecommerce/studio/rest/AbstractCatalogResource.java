package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.rest.linking.EntityResource;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;

/**
 * An abstract catalog object as a RESTful resource.
 */
public abstract class AbstractCatalogResource<Entity extends CommerceObject> implements EntityResource<Entity> {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractCatalogResource.class);

  private final CatalogAliasTranslationService catalogAliasTranslationService;

  static final String PATH_ID = "id";
  static final String PATH_SITE_ID = "siteId";
  static final String PATH_CATALOG_ALIAS = "catalogAlias";
  static final String PATH_WORKSPACE_ID = "workspaceId";
  public static final String QUERY_ID = "id";

  private WorkspaceId workspaceId = WORKSPACE_ID_NONE;

  protected AbstractCatalogResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  @Override
  public Entity getEntity(@NonNull Map<String, String> pathVariables) {
    if (!getStoreContext(pathVariables).isPresent()) {
      return null;
    }

    return doGetEntity(pathVariables);
  }

  protected abstract Entity doGetEntity(@NonNull Map<String, String> params);

  @GetMapping
  public AbstractCatalogRepresentation get(@PathVariable Map<String, String> params) {
    return getRepresentation(params);
  }

  protected abstract AbstractCatalogRepresentation getRepresentation(@NonNull Map<String, String> params);

  @NonNull
  @VisibleForTesting
  static String decodeId(@NonNull String id) {
    // At least, encoded `+` chars (`%2B`) must be decoded because
    // some program logic double escapes it in order to avoid `+`
    // characters being unescaped to SPACE characters.
    return id.replace("%2B", "+");
  }

  @NonNull
  protected Optional<StoreContext> getStoreContext(@NonNull Map<String, String> params) {
    String siteId = params.get(PATH_SITE_ID);
    CatalogAlias catalogAlias = CatalogAlias.ofNullable(params.get(PATH_CATALOG_ALIAS)).orElse(null);

    StoreContext originalContext = CurrentStoreContext.find().orElse(null);
    if (originalContext == null) {
      return Optional.empty();
    }

    StoreContextProvider storeContextProvider = originalContext.getConnection().getStoreContextProvider();

    StoreContextBuilder clonedContextBuilder = storeContextProvider
            .buildContext(originalContext)
            .withSiteId(siteId)
            .withWorkspaceId(workspaceId);

    if (catalogAlias != null && catalogAliasTranslationService != null) {
      Optional<CatalogId> catalogId = catalogAliasTranslationService
              .getCatalogIdForAlias(catalogAlias, originalContext);

      clonedContextBuilder = clonedContextBuilder
              .withCatalogId(catalogId.orElse(null))
              .withCatalogAlias(catalogAlias);
    }

    return Optional.of(clonedContextBuilder.build());
  }

  CatalogAliasTranslationService getCatalogAliasTranslationService() {
    return catalogAliasTranslationService;
  }
}
