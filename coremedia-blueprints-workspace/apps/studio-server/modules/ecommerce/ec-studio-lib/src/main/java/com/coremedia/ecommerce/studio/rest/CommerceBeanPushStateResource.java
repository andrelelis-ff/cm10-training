package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.push.PushService;
import com.coremedia.livecontext.ecommerce.push.PushState;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.rest.linking.EntityResource;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import java.util.Map;

import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_CATALOG_ALIAS;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_ID;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_SITE_ID;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_WORKSPACE_ID;
import static com.coremedia.ecommerce.studio.rest.CommerceBeanPushStateResource.URI_PATH;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.CATEGORY;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.SKU;

@RestController
@RequestMapping(value = URI_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CommerceBeanPushStateResource implements EntityResource<PushState> {

  private static final String PATH_TYPE = "type";
  static final String URI_PATH = "livecontext/pushState/{" + PATH_TYPE + "}/{" + PATH_SITE_ID + "}/{" + PATH_CATALOG_ALIAS + "}/{" + PATH_WORKSPACE_ID + "}/{" + PATH_ID + ":.+}";

  private final CommerceConnectionSupplier commerceConnectionSupplier;

  public CommerceBeanPushStateResource(@NonNull CommerceConnectionSupplier commerceConnectionSupplier) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  @Override
  public PushState getEntity(@NonNull Map<String, String> pathVariables) {
    String siteId = pathVariables.get(PATH_SITE_ID);

    CommerceConnection connection = commerceConnectionSupplier.findConnection(siteId).orElse(null);
    if (connection == null) {
      return null;
    }

    CurrentStoreContext.set(connection.getStoreContext());

    PushService pushService = connection.getPushService().orElse(null);
    if (pushService == null) {
      return null;
    }

    CatalogAlias catalogAlias = CatalogAlias.of(pathVariables.get(PATH_CATALOG_ALIAS));
    WorkspaceId workspaceId = WorkspaceId.of(pathVariables.get(PATH_WORKSPACE_ID));
    StoreContext entityStoreContext = getEntityStoreContext(workspaceId, catalogAlias, connection);

    String type = pathVariables.get(PATH_TYPE);
    String id = pathVariables.get(PATH_ID);
    CommerceId commerceId = getCommerceId(id, type, catalogAlias, connection.getIdProvider());
    if (commerceId == null) {
      return null;
    }

    CommerceBean bean = connection.getCommerceBeanFactory().createBeanFor(commerceId, entityStoreContext);

    return pushService.getPushState(CommerceIdFormatterHelper.format(bean.getId()), bean.getContext());
  }

  @NonNull
  private static StoreContext getEntityStoreContext(WorkspaceId workspaceId, CatalogAlias catalogAlias,
                                                    CommerceConnection connection) {
    StoreContext siteStoreContext = connection.getStoreContext();

    return connection.getStoreContextProvider()
            .buildContext(siteStoreContext)
            .withCatalogAlias(catalogAlias)
            .withWorkspaceId(workspaceId)
            .build();
  }

  @Nullable
  private static CommerceId getCommerceId(String id, String type, CatalogAlias catalogAlias,
                                          CommerceIdProvider idProvider) {
    CommerceId commerceId = null;
    if (type.equals(CATEGORY.type())) {
      commerceId = idProvider.formatCategoryId(catalogAlias, id);
    } else if (type.equals(PRODUCT.type())) {
      commerceId = idProvider.formatProductId(catalogAlias, id);
    } else if (type.equals(SKU.type())) {
      commerceId = idProvider.formatProductVariantId(catalogAlias, id);
    }
    return commerceId;
  }

  @Nullable
  @GetMapping
  public PushStateRepresentation get(@PathVariable Map<String, String> params) {
    PushState pushState = getEntity(params);

    if (pushState == null) {
      throw new NotFoundException("Push State bean not found");
    }

    return getRepresentation(pushState);
  }

  @NonNull
  static PushStateRepresentation getRepresentation(@NonNull PushState pushState) {
    String stateName = pushState.getState().name();

    Date date = pushState
            .getModificationDate()
            .map(ChronoZonedDateTime::toInstant)
            .map(Date::from)
            .orElse(null);

    return new PushStateRepresentation(stateName, date);
  }
}
