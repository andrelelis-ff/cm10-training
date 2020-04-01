package com.coremedia.livecontext.asset.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.asset.AssetSearchService;
import com.coremedia.livecontext.asset.AssetValidationService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.SKU;
import static java.util.Collections.emptyList;

public class AssetResolvingStrategyImpl implements AssetResolvingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(AssetResolvingStrategyImpl.class);

  private CommerceConnectionInitializer commerceConnectionInitializer;

  private AssetChanges assetChanges;
  private AssetSearchService assetSearchService;
  private AssetValidationService assetValidationService;

  /**
   * find assets of the given content type, reference id and site
   *
   * @param contentType the given content type
   * @param id          the reference id
   * @param site        the given site
   * @return the found assets
   */
  @NonNull
  public List<Content> findAssets(@NonNull String contentType, @NonNull CommerceId id, @NonNull Site site) {
    if (assetSearchService == null) {
      LOG.error("assetSearchService is not set, cannot find assets for {} in site {}", id, site.getName());
      return emptyList();
    }

    Optional<String> externalIdOptional = id.getExternalId();
    if (!externalIdOptional.isPresent()) {
      return emptyList();
    }
    String externalId = externalIdOptional.get();

    Set<Content> assets = resolveCachedAndIndexedAssets(contentType, externalId, site);
    List<Content> upToDate = selectUpToDate(externalId, site, assets);
    List<Content> referencedInContent = selectReferencedContent(externalId, upToDate);
    List<Content> validAssets = filterWithAssetValidationService(referencedInContent);

    sortByContentName(validAssets);

    if (validAssets.isEmpty() && isSkuId(id)) {
      //try to load referencedAssets from parent Product, if partNumber belongs to ProductVariant
      validAssets = findFallbackForProductVariant(id, site, contentType);
    }

    return validAssets;
  }

  @NonNull
  private Set<Content> resolveCachedAndIndexedAssets(@NonNull String contentType, @NonNull String externalId,
                                                     @NonNull Site site) {
    Collection<Content> cachedAssets = assetChanges.get(externalId, site);
    List<Content> indexedAssets = assetSearchService.searchAssets(contentType, externalId, site);

    List<Content> filteredCachedAssets = filterCachedAssets(contentType, cachedAssets);

    Set<Content> assets = new HashSet<>(indexedAssets);
    assets.addAll(filteredCachedAssets);
    return assets;
  }

  @NonNull
  private static List<Content> filterCachedAssets(@NonNull String contentType, @NonNull Collection<Content> cachedAssets) {
    List<Content> filteredCachedAssets = new ArrayList<>();
    for (Content cachedAsset : cachedAssets) {
      if (!cachedAsset.isDestroyed() && cachedAsset.getType().isSubtypeOf(contentType)) {
        filteredCachedAssets.add(cachedAsset);
      }
    }
    return filteredCachedAssets;
  }

  @NonNull
  private List<Content> selectUpToDate(@NonNull String id, @NonNull Site site, @NonNull Collection<Content> assets) {
    List<Content> upToDate = new ArrayList<>();
    for (Content asset : assets) {
      if (assetChanges.isUpToDate(asset, id, site)) {
        upToDate.add(asset);
      }
    }
    return upToDate;
  }

  @NonNull
  private List<Content> selectReferencedContent(@NonNull String id, @NonNull List<Content> upToDate) {
    List<Content> referencedInContent = new ArrayList<>();
    for (Content asset : upToDate) {
      List<String> externalIds = getExternalIds(asset);
      if (externalIds.contains(id)) {
        referencedInContent.add(asset);
      }
    }
    return referencedInContent;
  }

  @NonNull
  private List<Content> filterWithAssetValidationService(@NonNull List<Content> assets) {
    if (assetValidationService == null) {
      return assets;
    }
    return assetValidationService.filterAssets(assets);
  }

  private static void sortByContentName(@NonNull List<Content> validAssets) {
    Collections.sort(validAssets, (content1, content2) -> content1.getName().compareToIgnoreCase(content2.getName()));
  }

  /**
   * find fallback assets of the given content type, reference id of a sku and site
   *
   * @param id          the reference id of a sku
   * @param site        the given site
   * @param contentType the given content type
   * @return the found assets
   */
  @NonNull
  private List<Content> findFallbackForProductVariant(@NonNull CommerceId id,
                                                      @NonNull Site site,
                                                      @NonNull String contentType) {
    CommerceConnection commerceConnection = commerceConnectionInitializer.findConnectionForSite(site).orElse(null);
    if (commerceConnection == null) {
      return emptyList();
    }

    Product product = commerceConnection.getCatalogService()
            .findProductById(id, commerceConnection.getStoreContext());
    if (!(product instanceof ProductVariant)) {
      return emptyList();
    }

    Product parentProduct = ((ProductVariant) product).getParent();
    if (parentProduct == null) {
      return emptyList();
    }

    return findAssets(contentType, parentProduct.getReference(), site);
  }

  @VisibleForTesting
  boolean isSkuId(@NonNull CommerceId id) {
    return SKU.equals(id.getCommerceBeanType());
  }

  @VisibleForTesting
  List<String> getExternalIds(Content asset) {
    return CommerceReferenceHelper.getExternalIds(asset);
  }

  @Autowired
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Autowired
  public void setAssetChanges(AssetChanges assetChanges) {
    this.assetChanges = assetChanges;
  }

  @Autowired(required = false)
  public void setAssetValidationService(AssetValidationService assetValidationService) {
    this.assetValidationService = assetValidationService;
  }

  @Autowired(required = false)
  public void setAssetSearchService(AssetSearchService assetSearchService) {
    this.assetSearchService = assetSearchService;
  }
}
