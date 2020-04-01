package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoCommerceConnectionAvailable;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.common.ProductIdExtractor;
import com.coremedia.livecontext.asset.util.AssetHelper;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;

/**
 * Extracts product codes from XMP/IPTC Data and stores product references to struct property.
 */
public class BlobUploadXmpDataInterceptor extends ContentWriteInterceptorBase {

  private static final Logger LOG = LoggerFactory.getLogger(BlobUploadXmpDataInterceptor.class);

  private static final String ASSET_PRODUCT_IDS_ATTRIBUTE_NAME = "defaultProductIds";

  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final String blobProperty;
  private final AssetHelper assetHelper;

  BlobUploadXmpDataInterceptor(CommerceConnectionSupplier commerceConnectionSupplier,
                               String blobProperty,
                               AssetHelper assetHelper) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.blobProperty = blobProperty;
    this.assetHelper = assetHelper;
  }

  @Override
  public void intercept(@NonNull ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();

    if (!properties.containsKey(blobProperty)) {
      //not my turf
      return;
    }

    updateCMPicture(request, properties);
  }

  private void updateCMPicture(@NonNull ContentWriteRequest request, @NonNull Map<String, Object> properties) {
    Object value = properties.get(blobProperty);
    if (value instanceof Blob) {
      Blob blob = (Blob) value;

      CommerceConnection commerceConnection;
      try {
        commerceConnection = findCommerceConnection(request).orElse(null);

        if (commerceConnection == null) {
          LOG.debug("No commerce connection configured for site, won't extract XMP data from upload.");
          return;
        }
      } catch (NoCommerceConnectionAvailable e) {
        LOG.warn(
                "Commerce connection should be available for this site, but isn't; can't extract XMP data from upload.",
                e);
        return;
      }

      // Stand back, this gets ugly: Explicitly set the commerce
      // connection on the respective thread-local because the catalog
      // service needs it for now (and the commerce filter doesn't apply
      // in this upload scenario).
      CurrentStoreContext.set(commerceConnection.getStoreContext());

      List<String> productIds;
      try {
        productIds = getProductIds(commerceConnection, request, blob);
      } finally {
        CurrentStoreContext.remove();
      }

      properties.put(NAME_LOCAL_SETTINGS, assetHelper.updateCMPictureForExternalIds(request.getEntity(), productIds));
    } else if (value == null) {
      // delete blob action
      Struct result = assetHelper.updateCMPictureOnBlobDelete(request.getEntity());

      if (result != null) {
        properties.put(NAME_LOCAL_SETTINGS, result);
      }
    }
  }

  @NonNull
  private Optional<CommerceConnection> findCommerceConnection(@NonNull ContentWriteRequest request) {
    Content content = request.getEntity();

    if (content == null) {
      // Content is about to be created, but does not exist yet.
      // Fall back to the parent as it is expected to belong
      // to the same site as the content to be created.
      content = request.getParent();
    }

    return commerceConnectionSupplier.findConnection(content);
  }

  @NonNull
  private List<String> getProductIds(@NonNull CommerceConnection commerceConnection,
                                     @NonNull ContentWriteRequest request, @NonNull Blob blob) {
    List<String> productIds = new ArrayList<>();

    Iterable<String> xmpIds = getXmpIds(request, blob);
    for (String externalId : xmpIds) {
      Product product = retrieveProductOrVariant(externalId, commerceConnection);
      if (product != null) {
        productIds.add(CommerceIdFormatterHelper.format(product.getId()));
      } else {
        LOG.debug("Product id {} not found in catalog; XMP data will not be persisted.", externalId);
      }
    }

    return productIds;
  }

  @NonNull
  private static Iterable<String> getXmpIds(@NonNull ContentWriteRequest request, @NonNull Blob blob) {
    Object assetProductIds = request.getAttribute(ASSET_PRODUCT_IDS_ATTRIBUTE_NAME);

    if (assetProductIds != null) {
      return (Iterable) assetProductIds;
    } else {
      return ProductIdExtractor.extractProductIds(blob);
    }
  }

  @Nullable
  @VisibleForTesting
  Product retrieveProductOrVariant(String externalId, @NonNull CommerceConnection commerceConnection) {
    CommerceIdProvider idProvider = commerceConnection.getIdProvider();
    CatalogService catalogService = commerceConnection.getCatalogService();
    StoreContext storeContext = commerceConnection.getStoreContext();

    if (storeContext == null) {
      LOG.warn("Store context not available in commerce connection {}", commerceConnection);
      return null;
    }

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId productId = idProvider.formatProductId(catalogAlias, externalId);

    //the catalogservice allows to retrieve Categories, Products and/or ProductVariants by a single call of #findProductById
    Product result = catalogService.findProductById(productId, storeContext);
    if (result != null) {
      return result;
    } else {
      //for other catalog implementations an explicit request for ProductVariants
      productId = idProvider.formatProductVariantId(catalogAlias, externalId);
      result = catalogService.findProductVariantById(productId, storeContext);
    }
    return result;
  }
}
