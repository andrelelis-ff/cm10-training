package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.DOCUMENTTYPE;
import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.NAVIGATION_PATHS;
import static com.coremedia.blueprint.cae.search.SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT;
import static com.coremedia.livecontext.fragment.resolver.SearchTermExternalReferenceResolver.PREFIX;
import static com.coremedia.livecontext.fragment.resolver.SearchTermExternalReferenceResolver.QUERY_NAVIGATION_WITH_SEGMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchTermExternalReferenceResolverTest {

  private static final String SEGMENT_PATH = "search-landing-pages";
  private static final String FIELD = "keywords";
  private static final String CONTENT_TYPE = CMChannel.NAME;

  private static final int ROOT_CONTEXT_ID = 42;
  private static final int CONTEXT_ID = 48;
  private static final String SEARCH_TERM = "horse name";

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private Cache cache;

  @Mock
  private TreeRelation<Content> navigationTreeRelation;

  @Mock
  private SearchResultFactory searchResultFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Site site;

  @Mock
  private Content context;

  @Before
  public void setUp() {
    // mock content type without additional subtypes
    ContentType contentType = mock(ContentType.class, CONTENT_TYPE);
    when(contentType.getName()).thenReturn(CONTENT_TYPE);
    when(contentType.getSubtypes()).thenReturn(Collections.singleton(contentType));
    when(contentRepository.getContentType(CONTENT_TYPE)).thenReturn(contentType);

    QueryService queryService = mock(QueryService.class);
    when(contentRepository.getQueryService()).thenReturn(queryService);

    // mock that there's one channel below the site's root channel which has the configured segment
    Set<Content> contextSet = Collections.singleton(context);
    Content root = site.getSiteRootDocument();
    when(navigationTreeRelation.getChildrenOf(root)).thenReturn(contextSet);
    when(navigationTreeRelation.pathToRoot(context)).thenReturn(ImmutableList.of(root, context));
    when(navigationTreeRelation.pathToRoot(root)).thenReturn(ImmutableList.of(root));
    when(queryService.getContentFulfilling(contextSet, QUERY_NAVIGATION_WITH_SEGMENT, SEGMENT_PATH))
            .thenReturn(context);
    when(root.getId()).thenReturn(IdHelper.formatContentId(ROOT_CONTEXT_ID));
    when(context.getId()).thenReturn(IdHelper.formatContentId(CONTEXT_ID));

    // mock a cache by just calling evaluate directly
    when(cache.get(any(CacheKey.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return ((CacheKey) invocation.getArguments()[0]).evaluate(cache);
      }
    });

  }

  private SearchTermExternalReferenceResolver resolver() {
    SearchTermExternalReferenceResolver resolver = new SearchTermExternalReferenceResolver();
    configureResolver(resolver);
    resolver.afterPropertiesSet();
    return resolver;
  }

  private void configureResolver(SearchTermExternalReferenceResolver resolver) {
    resolver.setContentRepository(contentRepository);
    resolver.setCache(cache);
    resolver.setNavigationTreeRelation(navigationTreeRelation);
    resolver.setSearchResultFactory(searchResultFactory);

    resolver.setSegmentPath(SEGMENT_PATH);
    resolver.setField(FIELD);
    resolver.setContentType(CONTENT_TYPE);
  }

  // ---------------------------------------------------------------------- tests


  @Test(expected = IllegalArgumentException.class)
  public void testIllegalSegmentPath() {
    new SearchTermExternalReferenceResolver().setSegmentPath("/absolute");
  }

  @Test(expected = IllegalStateException.class)
  public void testIllegalContentType() {
    String unknownType = "unknown";
    when(contentRepository.getContentType(unknownType)).thenReturn(null);

    SearchTermExternalReferenceResolver resolver = new SearchTermExternalReferenceResolver();
    configureResolver(resolver);
    resolver.setContentType(unknownType);
    resolver.afterPropertiesSet();
  }

  @Test(expected = IllegalStateException.class)
  public void testNotInitialized() {
    SearchTermExternalReferenceResolver resolver = new SearchTermExternalReferenceResolver();
    FragmentParameters params = parametersFor(site, PREFIX + SEARCH_TERM);
    resolver.resolveExternalRef(params, site);
  }

  @Test
  public void testIncludeNull() {
    assertFalse(resolver().include(null));
  }

  @Test
  public void testWithoutExternalRef() {
    SearchTermExternalReferenceResolver resolver = resolver();
    FragmentParameters parameters = parametersFor(site, null);
    assertFalse(resolver.include(parameters));
    assertNull(resolver.resolveExternalRef(parameters, site));
  }

  @Test
  public void testOtherPrefix() {
    SearchTermExternalReferenceResolver resolver = resolver();
    FragmentParameters parameters = parametersFor(site, "something");
    assertFalse(resolver.include(parameters));
    assertNull(resolver.resolveExternalRef(parameters, site));
  }

  @Test
  public void testResolveUncached() {
    Content result = mock(Content.class);
    mockSearchResult(result);

    SearchTermExternalReferenceResolver resolver = resolver();
    resolver.setCacheForSeconds(0);
    assertResolve(resolver, result);
    verifyZeroInteractions(cache);
    verifyQueryBelowNavigationPath("\\/" + ROOT_CONTEXT_ID + "\\/" + CONTEXT_ID);
  }

  @Test
  public void testResolveCached() {
    Content result = mock(Content.class);
    mockSearchResult(result);

    assertResolve(resolver(), result);
    verify(cache).get(any(CacheKey.class));
    verifyQueryBelowNavigationPath("\\/" + ROOT_CONTEXT_ID + "\\/" + CONTEXT_ID);
  }

  @Test
  public void testResolveFirstResult() {
    Content result = mock(Content.class);
    mockSearchResult(result, mock(Content.class));

    assertResolve(resolver(), result);
    verify(cache).get(any(CacheKey.class));
    verifyQueryBelowNavigationPath("\\/" + ROOT_CONTEXT_ID + "\\/" + CONTEXT_ID);
  }

  @Test
  public void testResolveNoContext() {
    SearchTermExternalReferenceResolver resolver = new SearchTermExternalReferenceResolver();
    configureResolver(resolver);
    resolver.setSegmentPath("");
    resolver.afterPropertiesSet();

    Content result = mock(Content.class);
    mockSearchResult(result);
    assertResolve(resolver, result);
    verifyQueryBelowNavigationPath("\\/" + ROOT_CONTEXT_ID);
  }

  @Test
  public void testResolveContextNotFoundNoContextsBelowRoot() {
    when(navigationTreeRelation.getChildrenOf(site.getSiteRootDocument())).thenReturn(Collections.<Content>emptySet());
    Content result = mock(Content.class);
    mockSearchResult(result);
    assertResolve(resolver(), site.getSiteRootDocument());
  }

  @Test
  public void testResolveContextNotFoundSegmentNotFound() {
    SearchTermExternalReferenceResolver resolver = new SearchTermExternalReferenceResolver();
    configureResolver(resolver);
    resolver.setSegmentPath(SEGMENT_PATH + "/foo/bar");
    resolver.afterPropertiesSet();

    Content result = mock(Content.class);
    mockSearchResult(result);
    assertResolve(resolver, site.getSiteRootDocument());
  }

  @Test
  public void testResolveSiteHasNoRoot() {
    when(site.getSiteRootDocument()).thenReturn(null);
    Content result = mock(Content.class);
    mockSearchResult(result);
    FragmentParameters params = parametersFor(site, PREFIX + SEARCH_TERM);
    assertNull(resolver().resolveExternalRef(params, site));
  }

  @Test
  public void testResolveNotFound() {
    mockSearchResult();
    assertResolve(resolver(), context);
  }

  @Test
  public void testResolveNotFoundFallback() {
    SearchTermExternalReferenceResolver resolver = spy(new SearchTermExternalReferenceResolver());
    configureResolver(resolver);
    resolver.afterPropertiesSet();
    mockSearchResult();

    FragmentParameters params = parametersFor(site, PREFIX + SEARCH_TERM);
    LinkableAndNavigation linkableAndNavigation = resolver.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertEquals(context, linkableAndNavigation.getLinkable());
  }

  // ---------------------------------------------------------------------- helper methods

  private void assertResolve(@NonNull SearchTermExternalReferenceResolver resolver, @NonNull Content expectedLinkable) {
    FragmentParameters params = parametersFor(site, PREFIX + SEARCH_TERM);
    assertTrue(resolver.include(params));
    LinkableAndNavigation linkableAndNavigation = resolver.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertNull(linkableAndNavigation.getNavigation());
    assertEquals(expectedLinkable, linkableAndNavigation.getLinkable());
  }

  private static FragmentParameters parametersFor(Site site, String ref) {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;";
    FragmentParameters params = FragmentParametersFactory.create(url);
    params.setExternalReference(ref);
    return params;
  }

  private void verifyQueryBelowNavigationPath(@NonNull String navigationPath) {
    ArgumentCaptor<SearchQueryBean> queryCaptor = ArgumentCaptor.forClass(SearchQueryBean.class);
    verify(searchResultFactory).createSearchResultUncached(queryCaptor.capture());
    SearchQueryBean query = queryCaptor.getValue();
    assertTrue(query.isNotSearchableFlagIgnored());
    assertEquals(DYNAMICCONTENT, query.getSearchHandler());
    assertEquals(FIELD + ":\"" + SEARCH_TERM + '"', query.getQuery());
    assertEquals(ImmutableSet.of(
                    Condition.is(DOCUMENTTYPE, Value.anyOf(Collections.singleton('"' + CONTENT_TYPE + '"'))),
                    Condition.is(NAVIGATION_PATHS, Value.exactly(navigationPath))),
            ImmutableSet.copyOf(query.getFilters()));
  }

  private void mockSearchResult(Content... results) {
    SearchResultBean searchResultBean = mock(SearchResultBean.class);
    when(searchResultFactory.createSearchResultUncached(any(SearchQueryBean.class))).thenReturn(searchResultBean);

    List<CMLinkable> hits = new ArrayList<>();
    for (Content result: results) {
      CMLinkable linkableBean = mock(CMLinkable.class);
      when(linkableBean.getContent()).thenReturn(result);
      hits.add(linkableBean);
    }
    doReturn(hits).when(searchResultBean).getHits();
  }


}