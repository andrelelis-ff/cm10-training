package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.common.AMSettingKeys;
import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Factory that creates all kinds of download portal specific models ({@link CategoryOverview}, {@link AssetDetails}
 * and {@link PaginatedAssets}). The factory utilizes the search engine to easily retrieve additional information
 * about the number of assets within a category and its subcategories for example.
 * </p>
 *
 * @see #createAssetDetails(CMChannel, AMAsset, AMTaxonomy)
 * @see #createCategoryOverview(AMTaxonomy) ()
 */
public class DownloadPortalFactory {

  // package private access for testing purposes
  static final int ASSETS_PER_PAGE_DEFAULT = 12;

  private static final Comparator<AMAssetRendition> ASSET_RENDITION_COMPARATOR = new Comparator<AMAssetRendition>() {
    @Override
    public int compare(AMAssetRendition rendition1, AMAssetRendition rendition2) {
      if (rendition1.getAsset().equals(rendition2.getAsset())) {
        return rendition1.getName().compareTo(rendition2.getName());
      }
      return rendition1.getAsset().getTitle().compareTo(rendition2.getAsset().getTitle());
    }
  };

  private ContentRepository contentRepository;

  private MimeTypeService mimeTypeService;

  private SettingsService settingsService;

  private DataViewFactory dataViewFactory;

  private DownloadPortalSearchService downloadPortalSearchService;

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setDownloadPortalSearchService(DownloadPortalSearchService downloadPortalSearchService) {
    this.downloadPortalSearchService = downloadPortalSearchService;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }


  @Required
  public void setMimeTypeService(MimeTypeService mimeTypeService) {
    this.mimeTypeService = mimeTypeService;
  }


  /**
   * <p>
   * Creates the AssetDetails model based on the given asset and category. If the category is <code>null</code> then
   * the factory will use the asset's primary category.
   * </p>
   * <p>
   * If the category is set then the factory validates if it actually matches one of the asset's categories. Otherwise it
   * will return <code>null</code>.
   * </p>
   *
   * @param navigation the asset download portal root page
   * @param asset      the asset
   * @param category   the category in which the asset should be displayed or <code>null</code>
   * @return the {@link AssetDetails} bean or <code>null</code> if the given category does not match
   * @see AMAsset#getPrimaryCategory()
   */
  @Nullable
  public AssetDetails createAssetDetails(@NonNull CMChannel navigation, @NonNull AMAsset asset, @Nullable AMTaxonomy category) {
    // double check if the given category is really linked to the given asset to prevent URL manipulation
    // category may be null (required for preview)
    if(category != null && !category.getContent().isInProduction()) {
      return null;
    }

    if(!asset.getContent().isInProduction()) {
      return null;
    }

    if (category == null || asset.getAssetCategories().contains(category)) {
      AMTaxonomy actualCategory = category != null ? category : asset.getPrimaryCategory();
      AssetDetails assetDetails = new AssetDetails(asset, actualCategory);
      assetDetails.setMetadataProperties(getVisibleAssetMetadata(navigation, asset));
      return assetDetails;
    }
    return null;
  }

  public void prepareRenditionsDownload(List<AMAssetRendition> renditionsToDownload) {
    DownloadCollectionZipCacheKey zipFileCacheKey = new DownloadCollectionZipCacheKey(
            renditionsToDownload,
            contentRepository,
            mimeTypeService);

    // use getCache.get(zipFileCacheKey) to already start creating the zip file
    contentRepository.getConnection().getCache().get(zipFileCacheKey);
  }

  public File getPreparedDownload(List<AMAssetRendition> renditionsToDownload) {
    File file = null;
    DownloadCollectionZipCacheKey zipFileCacheKey = new DownloadCollectionZipCacheKey(
            renditionsToDownload,
            contentRepository,
            mimeTypeService);
    // only if the zip file was previously prepared it is present in the cache
    if (contentRepository.getConnection().getCache().peek(zipFileCacheKey).isPresent()) {
      file = contentRepository.getConnection().getCache().get(zipFileCacheKey);
    }
    return file;
  }

  /**
   * Returns visible metadata of the given asset.
   * Only metadata keys listed in {@link #getMetadataWhiteList(com.coremedia.blueprint.common.contentbeans.CMChannel)}
   * will be contained in the map.
   *
   * @param navigation the navigation
   * @param asset      the asset
   * @return the metadata to be shown
   */
  private Map<String, String> getVisibleAssetMetadata(@NonNull CMChannel navigation, @NonNull AMAsset asset) {
    Struct metadata = asset.getMetadata();
    if (metadata != null && !metadata.getProperties().isEmpty()) {
      Map<String, Object> assetMetadata = metadata.getProperties();
      List<String> whiteList = getMetadataWhiteList(navigation);
      if (whiteList != null) {
        return AMUtils.getPropertiesAsString(getFilteredMetaData(assetMetadata, whiteList));
      }
    }
    return Collections.emptyMap();
  }

  private static Map<String, Object> getFilteredMetaData(Map<String, Object> assetMetadata, List<String> whiteList) {
    // using LinkedHashMap to keep the order the properties were inserted
    Map<String, Object> filteredAssetMetadata = new LinkedHashMap<>();
    for (String key : whiteList) {
      if (assetMetadata.containsKey(key)) {
        filteredAssetMetadata.put(key, assetMetadata.get(key));
      }
    }
    return filteredAssetMetadata;
  }

  /**
   * Fetch the settings struct asset-management/download-portal/metadata
   * for the given navigation.
   *
   * @param navigation the navigation linking to the settings struct
   * @return the settings struct for the given navigation or null
   */
  @Nullable
  private List<String> getMetadataWhiteList(@NonNull CMChannel navigation) {
    ImmutableList<String> metadataHierarchy = ImmutableList.of(AMSettingKeys.ASSET_MANAGEMENT,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL_METADATA_PROPERTIES);
    //noinspection unchecked
    return (List<String>) settingsService.nestedSetting(metadataHierarchy, List.class, navigation);
  }

  /**
   * Fetch the settings struct asset-management/download-portal/assets-per-page
   * for the given navigation.
   *
   * @param navigation the navigation linking to the settings struct
   * @return the number of assets per page
   */
  @NonNull
  private Integer getAssetsPerPage(@NonNull CMChannel navigation) {
    ImmutableList<String> assetsPerPageInSettings = ImmutableList.of(AMSettingKeys.ASSET_MANAGEMENT,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL,
            AMSettingKeys.ASSET_MANAGEMENT_DOWNLOAD_PORTAL_ASSETS_PER_PAGE);
    Integer assetsPerPage = settingsService.nestedSetting(assetsPerPageInSettings, Integer.class, navigation);
    if (assetsPerPage == null || assetsPerPage < 0) {
      return ASSETS_PER_PAGE_DEFAULT;
    }
    return assetsPerPage;
  }

  /**
   * <p>
   * Creates the AMCategoryOverview model based on the given category. To create the {@link CategoryOverview} that
   * represents the entry point you can pass <code>null</code>.
   * </p>
   * <p>
   * The assets and the subcategories within the resulting bean are sorted alphabetically. Also, only the subcategory
   * that actually have assets are returned.
   * </p>
   *
   * @param category the category
   * @return the {@link CategoryOverview} bean for the given category.
   */
  @NonNull
  public CategoryOverview createCategoryOverview(@Nullable AMTaxonomy category) {
    return new CategoryOverview(category, downloadPortalSearchService.getSubCategories(category));
  }

  /**
   * <p>
   * Creates the AMSubjectOverview model based on the given subject taxonomy. To create the {@link TaxonomyOverview}
   * that represents the entry point you can pass <code>null</code>.
   * </p>
   * <p>
   * The assets within the resulting bean are sorted alphabetically.
   * </p>
   *
   * @param subjectTaxonomy the taxonomy used as subject
   * @return the {@link TaxonomyOverview} bean for the given taxonomy.
   */
  public TaxonomyOverview createSubjectOverview(CMTaxonomy subjectTaxonomy) {
    return new TaxonomyOverview(subjectTaxonomy);
  }


  public SearchOverview createSearchOverview(@NonNull String query) {
    return new SearchOverview(query);
  }

  public DownloadCollectionOverview createDownloadCollectionOverview(List<AMAssetRendition> renditions) {
    Collections.sort(renditions, ASSET_RENDITION_COMPARATOR);
    return new DownloadCollectionOverview(renditions);
  }

  /**
   * <p>
   * Creates the {@link PaginatedAssets} model based on the given parameters. To create the category assets object
   * of the root page pass <code>null</code> instead of the category.
   * </p>
   *
   * @param category   the category
   * @param navigation the navigation context of the download portal to access all kinds of settings
   * @param pageNo     the requested page number
   * @return the {@link PaginatedAssets} that represents the requested category assets.
   */
  @NonNull
  public PaginatedAssets createPaginatedCategoryAssets(@Nullable AMTaxonomy category, @NonNull CMChannel navigation, int pageNo) {
    if (category == null || !category.getContent().isInProduction()) {
      return new PaginatedAssets();
    }
    int assetsPerPage = getAssetsPerPage(navigation);
    SearchResultBean searchResultBean = downloadPortalSearchService.getAssetsForCategory(category, assetsPerPage, pageNo);

    return createPaginatedAssets(searchResultBean, pageNo, assetsPerPage);
  }

  /**
   * <p>
   * Creates the {@link PaginatedAssets} model based on the given parameters.
   * </p>
   *
   * @param subject    the subject taxonomy node
   * @param navigation the navigation context of the download portal to access all kinds of settings
   * @param pageNo     the requested page number
   * @return the {@link PaginatedAssets} that represents the requested category assets.
   */
  @NonNull
  public PaginatedAssets createPaginatedSubjectAssets(@Nullable CMTaxonomy subject,
                                                      @NonNull CMChannel navigation,
                                                      int pageNo) {
    if (subject == null || !subject.getContent().isInProduction()) {
      return new PaginatedAssets();
    }
    int assetsPerPage = getAssetsPerPage(navigation);
    SearchResultBean searchResultBean = downloadPortalSearchService.getAssetsForSubject(subject, assetsPerPage, pageNo);

    return createPaginatedAssets(searchResultBean, pageNo, assetsPerPage);
  }

  /**
   * <p>
   * Creates the {@link PaginatedAssets} model based on the given parameters.
   * </p>
   *
   * @param query    the query to use
   * @param navigation the navigation context of the download portal to access all kinds of settings
   * @param pageNo     the requested page number
   * @return the {@link PaginatedAssets} that represents the requested category assets.
   */
  @NonNull
  public PaginatedAssets createPaginatedSearchAssets(@NonNull String query,
                                                     @NonNull CMChannel navigation,
                                                     int pageNo) {
    int assetsPerPage = getAssetsPerPage(navigation);
    SearchResultBean searchResultBean = downloadPortalSearchService.searchForAssets(query, assetsPerPage, pageNo);
    return createPaginatedAssets(searchResultBean, pageNo, assetsPerPage);
  }

  public PaginatedAssets createEmptyPaginatedAssets() {
    return new PaginatedAssets();
  }

  @NonNull
  private List<AMAsset> createAssetsFromHits(@NonNull List<?> hits) {
    List<AMAsset> assets = new ArrayList<>(hits.size());
    for (Object o : hits) {
      if (o instanceof AMAsset) {
        assets.add((AMAsset) o);
      }
    }
    return dataViewFactory.loadAllCached(assets, null);
  }

  private PaginatedAssets createPaginatedAssets(SearchResultBean searchResultBean, int pageNo, int hitsPerPage) {
    List<AMAsset> assets = createAssetsFromHits(searchResultBean.getHits());
    int pageCount = (int) Math.ceil(searchResultBean.getNumHits() / (double) hitsPerPage);

    return new PaginatedAssets(assets, pageNo, pageCount, searchResultBean.getNumHits());
  }
}
