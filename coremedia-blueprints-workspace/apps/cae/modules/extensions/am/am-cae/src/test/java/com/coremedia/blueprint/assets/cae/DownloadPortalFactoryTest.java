package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadPortalFactoryTest {

  @InjectMocks
  private DownloadPortalFactory factory;

  @Mock
  private DownloadPortalSearchService downloadPortalSearchService;

  @Mock
  private DataViewFactory dataViewFactory;

  @Mock
  private SettingsService settingsService;

  @Mock
  private SearchResultBean resultBean;

  @Mock
  private AMTaxonomy category;

  @Mock
  private AMTaxonomy subTaxonomy1;

  @Mock
  private AMAsset asset1;

  @Mock
  private AMAsset asset2;

  @Mock
  private Content content;

  @Mock
  private Content categoryContent;

  @Mock
  private CMChannel navigation;

  @Mock
  private Struct struct;

  @Before
  public void setUp() {
    when(dataViewFactory.loadAllCached(anyList(), nullable(String.class))).then(returnsFirstArg());

    when(category.getContent()).thenReturn(categoryContent);
    when(categoryContent.isInProduction()).thenReturn(true);

    when(content.isInProduction()).thenReturn(true);

    when(category.getContent()).thenReturn(content);
    when(category.getContent().isInProduction()).thenReturn(true);

    when(asset1.getTitle()).thenReturn("asset1");
    when(asset2.getTitle()).thenReturn("asset2");
    when(asset1.getContent()).thenReturn(content);
    when(asset2.getContent()).thenReturn(content);

    when(asset1.getContent().isInProduction()).thenReturn(true);
    when(asset2.getContent().isInProduction()).thenReturn(true);
  }

  @Test
  public void testCreateSubjectOverview() {
    TaxonomyOverview subjectOverview = factory.createSubjectOverview(null);

    assertNotNull(subjectOverview);
    assertNull(subjectOverview.getTaxonomy());

    subjectOverview = factory.createSubjectOverview(category);
    assertNotNull(subjectOverview);
    assertEquals(category, subjectOverview.getTaxonomy());
  }

  @Test
  public void testCreateSearchOverview() {
    String query = "searchMe";
    SearchOverview searchOverview = factory.createSearchOverview(query);
    assertNotNull(searchOverview);
    assertEquals(query, searchOverview.getSearchTerm());
  }

  @Test
  public void testCreateCategoryOverview() {
    ImmutableList<AMTaxonomy> subCategories = ImmutableList.of(subTaxonomy1);
    doReturn(subCategories).when(downloadPortalSearchService).getSubCategories(category);

    CategoryOverview categoryOverview = factory.createCategoryOverview(category);

    assertNotNull(categoryOverview);
    assertEquals(category, categoryOverview.getCategory());
    assertEquals(subCategories, categoryOverview.getSubcategories());
  }

  @Test
  public void testCreateAssetDetails_assetDetailPageWithIllegalCategory_returnsNull() {
    AMTaxonomy illegalCategory = mock(AMTaxonomy.class, "Illegal Category");
    AMAsset requestedAsset = mock(AMAsset.class, "Requested Asset");
    when(requestedAsset.getContent()).thenReturn(content);
    when(illegalCategory.getContent()).thenReturn(categoryContent);
    when(requestedAsset.getAssetCategories()).thenReturn(Collections.singletonList(category));

    final AssetDetails assetDetails = factory.createAssetDetails(navigation, requestedAsset, illegalCategory);

    assertNull("Illegal category should result in null", assetDetails);
  }

  @Test
  public void testCreateAssetDetails_assetDetailPageWithCategory_returnsAssetDetails() {
    AMTaxonomy anotherCategory = mock(AMTaxonomy.class, "Another Category");
    AMAsset requestedAsset = mock(AMAsset.class, "Requested Asset");

    when(requestedAsset.getAssetCategories()).thenReturn(Arrays.asList(category, anotherCategory));
    List<String> whiteList = Collections.singletonList("test");
    when(settingsService.nestedSetting(anyList(), eq(List.class), any())).thenReturn(whiteList);
    Map<String, Object> metadataProperties = new HashMap<>();
    metadataProperties.put("test", "string");
    when(struct.getProperties()).thenReturn(metadataProperties);
    when(requestedAsset.getMetadata()).thenReturn(struct);
    when(requestedAsset.getContent()).thenReturn(content);
    when(anotherCategory.getContent()).thenReturn(categoryContent);
    final AssetDetails assetDetails = factory.createAssetDetails(navigation, requestedAsset, anotherCategory);

    assertNotNull("The assetDetails should not be null for legal params", assetDetails);
    assertTrue("The asset details needs to contain the requested asset and category",
            anotherCategory.equals(assetDetails.getCategory()) && requestedAsset.equals(assetDetails.getAsset()));
    assertEquals("Asset metadata properties should be set", metadataProperties, assetDetails.getMetadataProperties());
  }

  @Test
  public void testCreateAssetDetails_assetDetailPageWithNullCategory_returnsAssetWithPrimaryCategory() {
    AMAsset requestedAsset = mock(AMAsset.class, "Requested Asset");
    when(requestedAsset.getContent()).thenReturn(content);
    when(requestedAsset.getPrimaryCategory()).thenReturn(category);
    when(requestedAsset.getMetadata()).thenReturn(struct);

    final AssetDetails assetDetails = factory.createAssetDetails(navigation, requestedAsset, null);

    assertNotNull("The assetDetails should not be null for legal params", assetDetails);
    assertTrue("The asset details needs to contain the requested asset and category",
            category.equals(assetDetails.getCategory()) && requestedAsset.equals(assetDetails.getAsset()));

    verify(requestedAsset, times(1)).getPrimaryCategory();
  }

  @Test
  public void testCreatePaginatedAssets_null() {
    PaginatedAssets paginatedCategoryAssets = factory.createPaginatedCategoryAssets(null, navigation, 1);
    PaginatedAssets paginatedSubjectAssets = factory.createPaginatedSubjectAssets(null, navigation, 1);

    validatePaginatedAssets(paginatedCategoryAssets, 1, 1, Collections.emptyList());
    validatePaginatedAssets(paginatedSubjectAssets, 1, 1, Collections.emptyList());
  }

  @Test
  public void testCreatePaginatedAssetsForCategory() {
    int customAssetsPerPageDefault = 3;
    int requestedPage = 1;
    when(settingsService.nestedSetting(anyList(), eq(Integer.class), eq(navigation)))
            .thenReturn(customAssetsPerPageDefault);

    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(assets).when(resultBean).getHits();
    // force method to think that there are more results pretending that we have more results
    when(resultBean.getNumHits()).thenReturn(customAssetsPerPageDefault + 1L);
    when(downloadPortalSearchService.getAssetsForCategory(category, customAssetsPerPageDefault, requestedPage))
            .thenReturn(resultBean);

    PaginatedAssets paginatedCategoryAssets = factory.createPaginatedCategoryAssets(category, navigation, requestedPage);

    validatePaginatedAssets(paginatedCategoryAssets, requestedPage, 2, assets);
  }

  @Test
  public void testCreatePaginatedAssetsForSubject() {
    int customAssetsPerPageDefault = 3;
    int requestedPage = 1;
    CMTaxonomy subject = category; // Category is a subtype of taxonomy, so no new mock required
    when(settingsService.nestedSetting(anyList(), eq(Integer.class), eq(navigation)))
            .thenReturn(customAssetsPerPageDefault);

    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(assets).when(resultBean).getHits();
    // force method to think that there are more results pretending that we have more results
    when(resultBean.getNumHits()).thenReturn(customAssetsPerPageDefault + 1L);
    when(downloadPortalSearchService.getAssetsForSubject(subject, customAssetsPerPageDefault, requestedPage))
            .thenReturn(resultBean);

    PaginatedAssets paginatedSubjectAssets = factory.createPaginatedSubjectAssets(subject, navigation, requestedPage);

    validatePaginatedAssets(paginatedSubjectAssets, requestedPage, 2, assets);
  }

  @Test
  public void testCreatePaginatedSearchAssets() {
    String searchString = "searchMe";
    int requestedPage = 1;

    List<AMAsset> assets = Collections.emptyList();
    doReturn(assets).when(resultBean).getHits();
    // force method to think that there are more results pretending that we have more results
    when(resultBean.getNumHits()).thenReturn(DownloadPortalFactory.ASSETS_PER_PAGE_DEFAULT + 1L);
    when(downloadPortalSearchService.searchForAssets(searchString, DownloadPortalFactory.ASSETS_PER_PAGE_DEFAULT, requestedPage))
            .thenReturn(resultBean);

    PaginatedAssets paginatedAssets = factory.createPaginatedSearchAssets(searchString, navigation, requestedPage);

    validatePaginatedAssets(paginatedAssets, 1, 2, assets);
  }

  @Test
  public void testCreatePaginatedSearchAssetsNoResult() {
    String searchString = "searchMe";
    int requestedPage = 1;

    List<AMAsset> assets = Collections.emptyList();
    doReturn(assets).when(resultBean).getHits();
    when(resultBean.getNumHits()).thenReturn(0L);
    when(downloadPortalSearchService.searchForAssets(searchString, DownloadPortalFactory.ASSETS_PER_PAGE_DEFAULT, requestedPage))
            .thenReturn(resultBean);

    PaginatedAssets paginatedAssets = factory.createPaginatedSearchAssets(searchString, navigation, requestedPage);

    verify(downloadPortalSearchService).searchForAssets(searchString, DownloadPortalFactory.ASSETS_PER_PAGE_DEFAULT, requestedPage);

    validatePaginatedAssets(paginatedAssets, 1, 1, assets);
  }

  @Test
  public void testSortedDownloadCollection() {
    AMAssetRendition assetRendition1_original = getAssetRendidtion(asset1, "original");
    AMAssetRendition assetRendition1_web = getAssetRendidtion(asset1, "web");
    AMAssetRendition assetRendition2_original = getAssetRendidtion(asset2, "original");
    AMAssetRendition assetRendition2_web = getAssetRendidtion(asset2, "web");

    DownloadCollectionOverview downloadCollectionOverview = factory.createDownloadCollectionOverview(Arrays.asList(assetRendition2_web, assetRendition1_web, assetRendition1_original, assetRendition2_original));

    List<AMAssetRendition> sortedRenditions = downloadCollectionOverview.getRenditions();
    assertEquals(4, sortedRenditions.size());
    assertEquals(assetRendition1_original, sortedRenditions.get(0));
    assertEquals(assetRendition1_web, sortedRenditions.get(1));
    assertEquals(assetRendition2_original, sortedRenditions.get(2));
    assertEquals(assetRendition2_web, sortedRenditions.get(3));
  }

  @Test
  public void testEmptyPageAssets() {
    PaginatedAssets emptyPaginatedAssets = factory.createEmptyPaginatedAssets();

    assertNotNull(emptyPaginatedAssets);
    assertEquals(0, emptyPaginatedAssets.getAssets().size());
    assertEquals(0, emptyPaginatedAssets.getTotalCount());
  }

  private void validatePaginatedAssets(PaginatedAssets paginatedAssets, int currentPage, int pageCount, List<AMAsset> assets) {
    assertNotNull("createPaginatedCategoryAssets should never return null.", paginatedAssets);
    assertEquals("CategoryAssets should contain assets from search result", assets, paginatedAssets.getAssets());
    assertEquals("There should be two pages because numHits does not fit on one page", pageCount, paginatedAssets.getPageCount());
    assertEquals("Current page should be the fallback", currentPage, paginatedAssets.getCurrentPage());
  }

  private AMAssetRendition getAssetRendidtion(AMAsset asset, String name) {
    AMAssetRendition rendition = mock(AMAssetRendition.class);
    when(rendition.getName()).thenReturn(name);
    when(rendition.getAsset()).thenReturn(asset);

    return rendition;
  }
}
