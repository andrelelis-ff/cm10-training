package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CaeAssetSearchServiceTest {

  private static final String SEARCHABLE_DOCTYPE = "CMVisual";
  private static final String INDEXED_EXTERNAL_ID = "indexedExternalId";
  private static final int VALID_SITE_ID = 1;
  private static final String ABSOLUTE_ESCAPED_VALID_SITE_ID = "\\/" + VALID_SITE_ID;
  private static final long CACHE_FOR_IN_SECONDS = 300;

  @Spy
  private CaeAssetSearchService testling;

  @Mock
  private SearchQueryBean queryBean;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private SearchResultFactory searchResultFactory;

  @Mock
  private Site site;

  @Before
  public void setup() {
    initMocks(this);

    testling.setContentRepository(contentRepository);
    testling.setSearchResultFactory(searchResultFactory);

    //we don't care which site it is, it's important to have a site. Each site has a site id.
    doReturn(VALID_SITE_ID).when(testling).getSiteRootDocumentId(any(Site.class));
    doReturn(queryBean).when(testling).createQueryBean();
    //Do not test method getSubTypesOf here. It should have an extra test.
    doReturn(ImmutableList.of(SEARCHABLE_DOCTYPE)).when(testling).getSubTypesOf(SEARCHABLE_DOCTYPE);
  }

  @Test
  public void testSearchAssetsWithLimitDefault() {
    List<Content> expected = mockResult();

    List<Content> actual = testling.searchAssets(SEARCHABLE_DOCTYPE, INDEXED_EXTERNAL_ID, site);

    verifyQueryBean(INDEXED_EXTERNAL_ID);
    verify(queryBean).setLimit(500);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testSearchAssetsExplicitlySetLimit() {
    List<Content> expected = mockResult();
    testling.setResultLimit(200);

    List<Content> actual = testling.searchAssets(SEARCHABLE_DOCTYPE, INDEXED_EXTERNAL_ID, site);

    verifyQueryBean(INDEXED_EXTERNAL_ID);
    verify(queryBean).setLimit(200);
    assertThat(actual).isEqualTo(expected);
  }

  private List<Content> mockResult() {
    List<ContentBean> contentBeans = new ArrayList<>();
    List<Content> contents = filltWithBeans(contentBeans);
    SearchResultBean resultBean = createResultBean(contentBeans);
    when(searchResultFactory.createSearchResult(same(queryBean), eq(CACHE_FOR_IN_SECONDS))).thenReturn(resultBean);
    return contents;
  }

  /**
   * Just fill with mocked contents because we want a SOLR based result. We must assure that the results are the same
   * which are returned by solr. No other check makes sense
   *
   * @param contentBeans contentbean list
   * @return contents
   */
  private List<Content> filltWithBeans(List<ContentBean> contentBeans) {
    List<Content> contents = new ArrayList<>();
    int count = 5;
    for (int i = 0; i < count; i++) {
      Content content = mock(Content.class);
      ContentBean contentBean = mock(ContentBean.class);

      when(contentBean.getContent()).thenReturn(content);
      contentBeans.add(contentBean);
      contents.add(content);
    }
    return contents;
  }

  private SearchResultBean createResultBean(List<ContentBean> contentBeans) {
    SearchResultBean resultBean = new SearchResultBean();
    resultBean.setHits(contentBeans);
    return resultBean;
  }

  private void verifyQueryBean(String externalId) {
    verify(queryBean).setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
    verify(queryBean).setNotSearchableFlagIgnored(true);
    verify(queryBean).addFilter(eq(Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly(ABSOLUTE_ESCAPED_VALID_SITE_ID))));
    verify(queryBean).addFilter(eq(Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(ImmutableList.of(SEARCHABLE_DOCTYPE)))));
    verify(queryBean).setQuery("commerceitems:\"" + externalId + "\"");
  }
}
