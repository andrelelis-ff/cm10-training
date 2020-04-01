package com.coremedia.livecontext.asset.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.asset.AssetService;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class AssetServiceImpl implements AssetService {

  private static final String CONFIG_KEY_DEFAULT_PICTURE = "livecontext.assets.default.picture";

  private SitesService sitesService;
  private SettingsService settingsService;
  private AssetResolvingStrategy assetResolvingStrategy;

  @NonNull
  public CatalogPicture getCatalogPicture(String url) {
    return computeReferenceIdFromUrl(url)
            .map(referenceIdFromUrl -> getCatalogPicture(url, referenceIdFromUrl))
            .orElseGet(() -> new CatalogPicture(url, null));
  }

  @NonNull
  @Override
  public CatalogPicture getCatalogPicture(@NonNull String url, @NonNull CommerceId commerceId) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    com.coremedia.livecontext.ecommerce.link.LinkService linkService = connection.getLinkService().orElse(null);
    if (linkService == null) {
      return new CatalogPicture(url, null);
    }

    // Rewrite a given commerce url (make absolute and replace `{cmsHost}`)
    String imageUrl = linkService.getImageUrl(url, storeContext).orElse(null);

    // try to find assets related to the given commerce id
    List<Content> pictures = findPictures(commerceId, false);
    Content picture = pictures.stream().findFirst().orElse(null);
    return new CatalogPicture(imageUrl, picture);
  }

  @NonNull
  @Override
  public List<Content> findPictures(@NonNull CommerceId commerceId) {
    return findPictures(commerceId, true);
  }

  @NonNull
  @Override
  public List<Content> findPictures(@NonNull CommerceId commerceId, boolean withDefault) {
    Site site = findSite().orElse(null);
    if (site == null) {
      return emptyList();
    }

    List<Content> references = assetResolvingStrategy.findAssets("CMPicture", commerceId, site);
    if (!references.isEmpty()) {
      return references;
    }

    if (withDefault) {
      Content defaultPicture = getDefaultPicture(site);
      if (defaultPicture != null) {
        return singletonList(defaultPicture);
      }
    }

    return emptyList();
  }

  @NonNull
  @Override
  public List<Content> findVisuals(@NonNull CommerceId id) {
    return findVisuals(id, true);
  }

  @NonNull
  @Override
  public List<Content> findVisuals(@NonNull CommerceId commerceId, boolean withDefault) {
    Site site = findSite().orElse(null);
    if (site == null) {
      return emptyList();
    }

    List<Content> visuals = assetResolvingStrategy.findAssets("CMVisual", commerceId, site);

    if (withDefault && visuals.isEmpty()) {
      Content defaultPicture = getDefaultPicture(site);
      if (defaultPicture != null) {
        return singletonList(defaultPicture);
      }
    }

    return filterSpinners(visuals);
  }

  @NonNull
  private static List<Content> filterSpinners(@NonNull List<Content> allVisuals) {
    Set<Content> picturesInSpinners = extractPicturesInSpinners(allVisuals);
    return removePicturesInSpinners(allVisuals, picturesInSpinners);
  }

  @NonNull
  private static List<Content> removePicturesInSpinners(@NonNull List<Content> allVisuals,
                                                        @NonNull Set<Content> picturesInSpinners) {
    return allVisuals.stream()
            .filter(visual -> !picturesInSpinners.contains(visual))
            .collect(toList());
  }

  @NonNull
  private static Set<Content> extractPicturesInSpinners(@NonNull List<Content> allVisuals) {
    return allVisuals.stream()
            .filter(visual -> visual.getType().isSubtypeOf("CMSpinner"))
            .flatMap(spinner -> ((List<Content>) spinner.getList("sequence")).stream())
            .collect(toSet());
  }

  @NonNull
  @Override
  public List<Content> findDownloads(@NonNull CommerceId commerceId) {
    return findSite()
            .map(site -> assetResolvingStrategy.findAssets("CMDownload", commerceId, site))
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  private Optional<Site> findSite() {
    return CurrentStoreContext.find()
            .map(StoreContext::getSiteId)
            .flatMap(sitesService::findSite);
  }

  @Nullable
  @Override
  public Content getDefaultPicture(@NonNull Site site) {
    return settingsService.getSetting(CONFIG_KEY_DEFAULT_PICTURE, Content.class, site.getSiteRootDocument())
            .orElse(null);
  }

  @NonNull
  private static Optional<CommerceId> computeReferenceIdFromUrl(@Nullable String url) {
    if (StringUtils.isBlank(url)) {
      return Optional.empty();
    }

    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();

    CommerceIdProvider idProvider = connection.getIdProvider();

    String partNumber = parsePartNumberFromUrl(url).orElse(null);
    if (partNumber == null) {
      return Optional.empty();
    }

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    if (url.contains(CATEGORY_URI_PREFIX)) {
      CommerceId categoryId = idProvider.formatCategoryId(catalogAlias, partNumber);
      return Optional.of(categoryId);
    }

    if (url.contains(PRODUCT_URI_PREFIX)) {
      CommerceId productId = idProvider.formatProductId(catalogAlias, partNumber);
      return Optional.of(productId);
    }

    return Optional.empty();
  }

  @NonNull
  private static Optional<String> parsePartNumberFromUrl(@NonNull String urlStr) {
    int index = urlStr.lastIndexOf('.');
    if (index < 0) {
      return Optional.empty();
    }

    String fileName = urlStr.substring(0, index);
    index = fileName.lastIndexOf('/');
    if (index < 0) {
      return Optional.empty();
    }

    String partNumber = fileName.substring(index + 1);
    return Optional.of(partNumber);
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Autowired
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Autowired
  public void setAssetResolvingStrategy(AssetResolvingStrategy assetResolvingStrategy) {
    this.assetResolvingStrategy = assetResolvingStrategy;
  }
}
