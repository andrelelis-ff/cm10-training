package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolrSegmentResolverTest {

  private static final int CACHE_FOR = 5;

  private SolrSegmentResolver resolver = new SolrSegmentResolver();

  private final SearchResultFactory resultFactory = mock(SearchResultFactory.class);
  private final SearchResultBean searchResult = new SearchResultBean();

  @Before
  public void setUp() {
    when(resultFactory.createSearchResult(any(SearchQueryBean.class), eq((long) CACHE_FOR))).thenReturn(searchResult);

    resolver.setSearchResultFactory(resultFactory);
    resolver.setCacheForSeconds(CACHE_FOR);
  }

  @Test
  public void testResolveNotFound() {
    assertNull(resolver.resolveSegment(42, "foo", Object.class));
  }

  @Test
  public void testResolveTypeNotFound() {
    searchResult.setHits(Collections.singletonList(new Object()));
    assertNull(resolver.resolveSegment(42, "foo", String.class));
  }

  @Test
  public void testResolveFirst() {
    searchResult.setHits(Arrays.asList("A", "B"));
    assertEquals("A", resolver.resolveSegment(42, "foo", String.class));
  }

  @Test
  public void testResolve() {
    searchResult.setHits(Collections.singletonList("A"));
    assertEquals("A", resolver.resolveSegment(42, "fo\"o", String.class));

    ArgumentCaptor<SearchQueryBean> captor = ArgumentCaptor.forClass(SearchQueryBean.class);
    verify(resultFactory).createSearchResult(captor.capture(), anyLong());
    SearchQueryBean query = captor.getValue();

    assertEquals(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT, query.getSearchHandler());
    assertTrue(query.isNotSearchableFlagIgnored());
    assertEquals("segment:\"fo\\\"o\" AND contexts:42", query.getQuery());
  }
}