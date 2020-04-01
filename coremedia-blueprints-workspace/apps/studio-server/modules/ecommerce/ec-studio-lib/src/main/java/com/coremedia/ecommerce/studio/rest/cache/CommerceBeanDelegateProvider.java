package com.coremedia.ecommerce.studio.rest.cache;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdBuilder;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Helper class to provide maps that can be used as delegates for commerce beans satisfying the needs of
 * Studio REST resource linker.
 */
class CommerceBeanDelegateProvider {

  private static final String SITE_ID = "{siteId:.*}";
  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of("{workspaceId:.*}");
  private static final String ID = "{id:.*}";
  private static final CatalogAlias CATALOG_ALIAS_TEMPLATE_VAR = CatalogAlias.of("CATALOG_ALIAS_TEMPLATE_VAR");
  private static final String CATALOG_ALIAS = "{catalogAlias:.*}";

  private static final CommerceId TEMPLATE_COMMERCE_ID = CommerceIdBuilder
          .builder(Vendor.of("none"), "none", BaseCommerceBeanType.CATALOG)
          .withExternalId(ID)
          .withCatalogAlias(CATALOG_ALIAS_TEMPLATE_VAR)
          .build();

  private static final ImmutableMap<String, Object> COMMERCE_BEAN_DELEGATE = ImmutableMap.of(
          "id", TEMPLATE_COMMERCE_ID,
          "externalId", ID
  );

  private CommerceBeanDelegateProvider() {
  }

  @NonNull
  static Map<String, Object> get() {
    StoreContext storeContext = createStoreContext();
    return ImmutableMap.<String, Object>builder()
            .putAll(COMMERCE_BEAN_DELEGATE)
            .put("context", storeContext)
            .build();
  }

  @NonNull
  static StoreContext createStoreContext() {
    CommerceConnection connection = new BaseCommerceConnection();
    return StoreContextBuilderImpl.from(connection, SITE_ID)
            .withWorkspaceId(WORKSPACE_ID)
            .build();
  }

  @NonNull
  static String postProcess(@NonNull String commerceBeanUri) {
    return commerceBeanUri.replace(CATALOG_ALIAS_TEMPLATE_VAR.value(), CATALOG_ALIAS);
  }

  @NonNull
  static String forEncodedExternalId(@NonNull String commerceBeanUri, @NonNull CommerceId commerceId) {
    return commerceId.getExternalId()
            .map(CommerceBeanDelegateProvider::encodePartNumber)
            .map(encodedPartNumber -> commerceBeanUri.replace(ID, encodedPartNumber))
            .orElse(commerceBeanUri);
  }

  @NonNull
  @VisibleForTesting
  static String encodePartNumber(@NonNull String partNumber) {
    return UriUtils.encodePath(partNumber, StandardCharsets.UTF_8);
  }
}
