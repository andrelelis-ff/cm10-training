package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.facet.FacetResult;
import com.coremedia.blueprint.cae.search.facet.FacetValue;
import com.coremedia.blueprint.cae.search.solr.SolrQueryBuilder;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadPortalSearchServiceTest {

  public static final int ASSETS_PER_PAGE = 12;
  @InjectMocks
  private  DownloadPortalSearchService searchService;

  @Mock
  private SearchResultFactory searchResultFactory;
  @Mock
  private ContentBeanFactory contentBeanFactory;
  @Mock
  private ContentRepository contentRepository;
  @Mock
  private DataViewFactory dataViewFactory;

  @Mock
  private SearchQueryBean queryBean;
  @Mock
  private FacetValue facet;
  @Mock
  private FacetValue facet2;

  @Mock
  private SearchResultBean resultBean;

  @Mock
  private AMTaxonomy category;

  @Mock
  private AMTaxonomy subTaxonomy1;
  @Mock
  private AMTaxonomy subTaxonomy2;

  @Mock
  private AMAsset asset1;
  @Mock
  private AMAsset asset2;

  @Mock
  private Content content;

  @Mock
  private Content content2;

  @Mock
  private ContentType concreteAssetType;

  @Before
  public void setUp() {
    searchService = spy(searchService);
    searchService.setCacheTimeInSecs(60);
    searchService.setPreview(false);

    when(facet.getCount()).thenReturn(2l);
    when(facet.getValue()).thenReturn("0/111");
    when(facet2.getCount()).thenReturn(2l);
    when(facet2.getValue()).thenReturn("0/112");
    when(contentRepository.getContent("111")).thenReturn(content);
    when(contentRepository.getContent("112")).thenReturn(content2);

    when(contentBeanFactory.createBeanFor(content, AMTaxonomy.class)).thenReturn(subTaxonomy1);
    when(contentBeanFactory.createBeanFor(content2, AMTaxonomy.class)).thenReturn(subTaxonomy2);

    when(subTaxonomy1.getValue()).thenReturn("Z");
    when(subTaxonomy2.getValue()).thenReturn("A");

    when(category.getContentId()).thenReturn(123);
    when(dataViewFactory.loadCached(any(), nullable(String.class))).then(returnsFirstArg());

    doReturn( queryBean )
            .when(searchService)
            .createSearchQueryBean();

    when(searchResultFactory.createSearchResult(any(SearchQueryBean.class), anyLong())).thenReturn(resultBean);
  }

  @Test
  public void testAfterPropertiesSet_containsConcreteAssetType() {
    ContentType assetType = mock(ContentType.class);
    when(assetType.getSubtypes()).thenReturn(Collections.singleton(concreteAssetType));
    when(concreteAssetType.isConcrete()).thenReturn(true);
    when(concreteAssetType.getName()).thenReturn("AssetType");
    when(contentRepository.getContentType(AMAsset.NAME)).thenReturn(assetType);

    try {
      searchService.afterPropertiesSet();
    } catch (Exception e) {
      fail("afterPropertiesSet should not throw an exception but: " + e.getMessage());
    }
    assertNotNull(searchService.assetTypes);
    assertTrue(searchService.assetTypes.size() == 1 && searchService.assetTypes.contains(concreteAssetType.getName()));
  }

  @Test
  public void testGetSubCategoriesForRoot() {
    Map<String, Collection<FacetValue>> searchResult = new HashMap<>();
    List<FacetValue> facets = ImmutableList.of(facet, facet2);
    searchResult.put(DownloadPortalSearchService.ASSETHIERARCHY_SOLR_FIELD, facets);
    FacetResult facetResult = new FacetResult(searchResult);
    when(resultBean.getFacetResult()).thenReturn(facetResult);

    List<Subcategory> subCategories = searchService.getSubCategories(null);

    assertEquals(2, subCategories.size());
    AMTaxonomy category1 = subCategories.get(0).getCategory();
    AMTaxonomy category2 = subCategories.get(1).getCategory();
    assertTrue(category1.equals(subTaxonomy1) || category1.equals(subTaxonomy2));
    assertTrue(category2.equals(subTaxonomy1) || category2.equals(subTaxonomy2));

    verify(queryBean).setFacetPrefix(DownloadPortalSearchService.getPrefixedCategoryPath(null));
    verify(queryBean).setFacetFields(Collections.singletonList(DownloadPortalSearchService.ASSETHIERARCHY_SOLR_FIELD));
  }

  @Test
  public void testGetSubCategoriesForCategory() {
    Map<String, Collection<FacetValue>> searchResult = new HashMap<>();
    List<FacetValue> facets = ImmutableList.of(facet, facet2);
    searchResult.put(DownloadPortalSearchService.ASSETHIERARCHY_SOLR_FIELD, facets);
    FacetResult facetResult = new FacetResult(searchResult);
    when(resultBean.getFacetResult()).thenReturn(facetResult);

    List<Subcategory> subCategories = searchService.getSubCategories(category);

    assertEquals(2, subCategories.size());
    AMTaxonomy category1 = subCategories.get(0).getCategory();
    AMTaxonomy category2 = subCategories.get(1).getCategory();
    assertTrue(category1.equals(subTaxonomy1) || category1.equals(subTaxonomy2));
    assertTrue(category2.equals(subTaxonomy1) || category2.equals(subTaxonomy2));

    verify(queryBean).setFacetPrefix(DownloadPortalSearchService.getPrefixedCategoryPath(category));
    verify(queryBean).setFacetFields(Collections.singletonList(DownloadPortalSearchService.ASSETHIERARCHY_SOLR_FIELD));
  }

  @Test
  public void testAssetsForCategory() {
    int requestedPage = 1;
    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(assets).when(resultBean).getHits();

    ArgumentCaptor<Condition> conditionArgumentCaptor = ArgumentCaptor.forClass(Condition.class);

    SearchResultBean searchResultBean = searchService.getAssetsForCategory(category, ASSETS_PER_PAGE, requestedPage);

    assertEquals(assets, searchResultBean.getHits());
    assertEquals(searchResultBean, resultBean);

    verify(queryBean, atLeast(2)).addFilter(conditionArgumentCaptor.capture());
    checkForFilter(conditionArgumentCaptor.getAllValues(), SearchConstants.FIELDS.DOCUMENTTYPE.toString(), null);
    checkForFilter(conditionArgumentCaptor.getAllValues(), DownloadPortalSearchService.ASSETTAXONOMY_SOLR_FIELD, "" + category.getContentId());
  }

  @Test
  public void testFallbackForEmptyPageForAssetsForCategory() {
    int requestedPage = 2;
    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(Collections.emptyList()).doReturn(assets).when(resultBean).getHits();

    ArgumentCaptor<Condition> conditionArgumentCaptor = ArgumentCaptor.forClass(Condition.class);

    SearchResultBean searchResultBean = searchService.getAssetsForCategory(category, ASSETS_PER_PAGE, requestedPage);

    assertEquals(assets, searchResultBean.getHits());
    assertEquals(searchResultBean, resultBean);

    verify(queryBean, atLeast(2)).addFilter(conditionArgumentCaptor.capture());
    checkForFilter(conditionArgumentCaptor.getAllValues(), SearchConstants.FIELDS.DOCUMENTTYPE.toString(), null);
    checkForFilter(conditionArgumentCaptor.getAllValues(), DownloadPortalSearchService.ASSETTAXONOMY_SOLR_FIELD, "" + category.getContentId());
  }

  @Test
  public void testAssetsForSubject() {
    int requestedPage = 1;

    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(assets).when(resultBean).getHits();

    ArgumentCaptor<Condition> conditionArgumentCaptor = ArgumentCaptor.forClass(Condition.class);

    SearchResultBean searchResultBean = searchService.getAssetsForSubject(category, ASSETS_PER_PAGE, requestedPage);

    assertEquals(assets, searchResultBean.getHits());
    assertEquals(searchResultBean, resultBean);

    verify(queryBean, atLeast(3)).addFilter(conditionArgumentCaptor.capture());
    checkForFilter(conditionArgumentCaptor.getAllValues(), SearchConstants.FIELDS.DOCUMENTTYPE.toString(), null);
    checkForFilter(conditionArgumentCaptor.getAllValues(), DownloadPortalSearchService.ASSETTAXONOMY_SOLR_FIELD, SolrQueryBuilder.FIELD_SET_ANY_VALUE);
    checkForFilter(conditionArgumentCaptor.getAllValues(), SearchConstants.FIELDS.SUBJECT_TAXONOMY.toString(), "" + category.getContentId());
  }

  @Test
  public void testSearchForAssets() {
    String searchString = "searchMe";
    int requestedPage = 1;

    List<AMAsset> assets = Arrays.asList(asset1, asset2);
    doReturn(assets).when(resultBean).getHits();

    ArgumentCaptor<Condition> conditionArgumentCaptor = ArgumentCaptor.forClass(Condition.class);

    SearchResultBean searchResultBean = searchService.searchForAssets(searchString, ASSETS_PER_PAGE, requestedPage);

    assertEquals(assets, searchResultBean.getHits());
    assertEquals(searchResultBean, resultBean);

    verify(queryBean, atLeast(2)).addFilter(conditionArgumentCaptor.capture());
    checkForFilter(conditionArgumentCaptor.getAllValues(), SearchConstants.FIELDS.DOCUMENTTYPE.toString(), null);
    checkForFilter(conditionArgumentCaptor.getAllValues(), DownloadPortalSearchService.ASSETTAXONOMY_SOLR_FIELD, SolrQueryBuilder.FIELD_SET_ANY_VALUE);
  }

  private void checkForFilter(List<Condition> conditionList, @NonNull String field, String valuePart) {
    boolean found = false;

    for (Condition condition: conditionList) {
      if (field.equals(condition.getField())) {
        found = valuePart == null || condition.getValue().toString().contains(valuePart);
      }
      if (found) {
        break;
      }
    }

    assertTrue(String.format("Need to find field %s with part of value %s in usage of filters", field, valuePart), found);
  }
}
