package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.cae.search.ValueAndCount;
import com.coremedia.blueprint.cae.search.facet.FacetResult;
import com.coremedia.blueprint.cae.search.facet.FacetValue;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestions;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cache.Cache;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.coremedia.blueprint.cae.action.search.SearchServiceTest.LocalConfig.PROFILE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CACHE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.ID_PROVIDER;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link SearchService}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SearchServiceTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
@TestPropertySource(properties = {
        "repository.params.contentxml=classpath:/com/coremedia/blueprint/cae/action/search/searchservice/content.xml",
})
public class SearchServiceTest {
  @Configuration
  @ComponentScan("com.coremedia.cap.common.xml")
  @ImportResource(
          value = {
                  CACHE,
                  CONTENT_BEAN_FACTORY,
                  ID_PROVIDER,
                  "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
                  "classpath:/framework/spring/blueprint-search.xml",
                  "classpath:/framework/spring/blueprint-contentbeans.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({ContentTestConfiguration.class})
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "SearchServiceTest";
  }

  private static final String TERM_NAME = "london";
  private static final Long TERM_COUNT = 1L;
  private static final int ROOT_NAVIGATION_ID = 124;
  private static final int ARTICLE_ID = 4;
  private static final String SEARCHFORMBEAN_QUERY = "sfbQuery";
  private static final String DOCUMENT_TYPE = "CMNavigation";

  private final SearchFormBean searchFormBean = new SearchFormBean();
  private final Collection<String> docTypes = singletonList(DOCUMENT_TYPE);

  @Inject
  private SearchService testling;
  private CMChannel navigation;
  private Page page;

  @Inject
  private Cache cache;

  @Inject
  private ContentTestHelper contentTestHelper;
  @Inject
  private SitesService sitesService;

  @Before
  public void setUp() throws Exception {
    searchFormBean.setChannelId(String.valueOf(ROOT_NAVIGATION_ID));
    searchFormBean.setDocType(DOCUMENT_TYPE);
    searchFormBean.setQuery(SEARCHFORMBEAN_QUERY);

    CMArticle article = contentTestHelper.getContentBean(ARTICLE_ID);
    navigation = contentTestHelper.getContentBean(ROOT_NAVIGATION_ID);

    page = new PageImpl(navigation, article, true, sitesService, cache, null, null, null);
  }

  @Test
  public void testSearch() {

    //make validator for the LocalSearchResultFactory.
    Validator validator = new Validator() {
      @Override
      public void validate(SearchQueryBean searchQueryBean) {
        assertNotNull("searchQueryBean is null", searchQueryBean);

        //test conditions
        Condition channelCondition = Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + ROOT_NAVIGATION_ID));
        Condition docTypeCondition = Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.exactly(searchFormBean.getDocTypeEscaped()));

        Collection<Condition> expectedConditions = new ArrayList<>();
        expectedConditions.add(channelCondition);
        expectedConditions.add(docTypeCondition);

        for (Condition condition : expectedConditions) {
          assertTrue("condition not found in searchQueryBean: " + condition.toString(), searchQueryBean.getFilters().contains(condition));
        }
      }
    };

    //overwrite SearchResultFactory with local implementation
    testling.setResultFactory(new LocalSearchResultFactory(validator));

    //result does not matter, all assertions are made in the validator.
    testling.search(page, searchFormBean, docTypes);
  }

  @Test
  public void testSearchTopics() {
    // as defined in content.xml
    final List<String> taxonomyDocumentTypes = ImmutableList.of("CMTaxonomy", "CMLocTaxonomy");
    final CMTaxonomy subjectTaxonomy = contentTestHelper.getContentBean(130);
    final CMLocTaxonomy locTaxonomy = contentTestHelper.getContentBean(132);
    final String taxonomyQuery = "bar";

    testling.setResultFactory(new SearchResultFactory() {
      @Override
      public SearchResultBean createSearchResult(SearchQueryBean searchInput, long cacheForInSeconds) {
        return createSearchResultUncached(searchInput);
      }

      @Override
      public SearchResultBean createSearchResultUncached(SearchQueryBean searchInput) {
        assertNotNull(searchInput);
        assertEquals(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT, searchInput.getSearchHandler());
        SearchResultBean result = new SearchResultBean();

        if ((SearchConstants.FIELDS.TEASER_TITLE + ":" + taxonomyQuery).equals(searchInput.getQuery())) {
          // query for taxonomies matching the query string and doctypes returns subjectTaxonomy and locTaxonomy
          Condition typeCondition = Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(taxonomyDocumentTypes));
          assertEquals(ImmutableList.of(typeCondition), ImmutableList.copyOf(searchInput.getFilters()));
          result.setHits(ImmutableList.of(subjectTaxonomy, locTaxonomy));
          result.setNumHits(2);
        } else if ("subjecttaxonomy:(130 OR 132) OR locationtaxonomy:(130 OR 132)".equals(searchInput.getQuery())) {
          // only the subjectTaxonomy is actually used (in addition to some other taxonomies)
          Condition rootCondition = Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + ROOT_NAVIGATION_ID));
          assertEquals(ImmutableList.of(rootCondition), ImmutableList.copyOf(searchInput.getFilters()));
          assertEquals(ImmutableSet.of("subjecttaxonomy", "locationtaxonomy"), ImmutableSet.copyOf(searchInput.getFacetFields()));
          assertEquals(1, searchInput.getFacetMinCount());
          assertEquals(0, searchInput.getLimit());
          result.setFacetResult(new FacetResult(ImmutableMap.of(
                  "subjecttaxonomy", ImmutableList.of(new FacetValue("subjecttaxonomy", "130", 3), new FacetValue("subjecttaxonomy", "134", 1)),
                  "locationtaxonomy", ImmutableList.of(new FacetValue("locationtaxonomy", "136", 1))
          )));
        } else {
          fail("unexpected query");
        }

        return result;
      }
    });

    SearchFormBean search = new SearchFormBean();
    search.setChannelId(String.valueOf(ROOT_NAVIGATION_ID));
    search.setQuery(taxonomyQuery);
    SearchResultBean searchResultBean = testling.searchTopics(navigation, search, taxonomyDocumentTypes, null);
    assertEquals(ImmutableSet.of(subjectTaxonomy), ImmutableSet.copyOf(searchResultBean.getHits()));
    assertEquals(1, searchResultBean.getNumHits());
  }

  @Test
  public void testAutocompleteSuggestions() {

    //make validator for the LocalSearchResultFactory.
    Validator validator = new Validator() {
      @Override
      public void validate(SearchQueryBean searchQueryBean) {
        assertNotNull("searchQueryBean is null", searchQueryBean);
        Condition condition = searchQueryBean.getFilters().get(0);
        assertEquals("values does not match", "\\/" + ROOT_NAVIGATION_ID, condition.getValue().getValue().toArray()[0]);
        assertEquals("op does not match", Value.Operators.AND, condition.getValue().getOp());
        assertEquals(SearchQueryBean.SEARCH_HANDLER.SUGGEST, searchQueryBean.getSearchHandler());
      }
    };
    //overwrite SearchResultFactory with local implementation
    testling.setResultFactory(new LocalSearchResultFactory(validator));

    Suggestions suggestions = testling.getAutocompleteSuggestions(String.valueOf(ROOT_NAVIGATION_ID), TERM_NAME,
            docTypes);

    assertEquals("originalTerm does not match", TERM_NAME, suggestions.get(0).getValue());
    assertEquals("label does not match", TERM_NAME + " (" + TERM_COUNT + ")", suggestions.get(0).getLabel());

  }

  @Test
  public void testEmptySearch() {

    //create bean with empty query
    SearchFormBean localSearchFormBean = new SearchFormBean();
    localSearchFormBean.setQuery("");

    assertNull("return null on empty query", testling.searchTopics(navigation, localSearchFormBean, docTypes, Collections.<String>emptyList()));
    assertNull("return null on empty query", testling.search(page, localSearchFormBean, docTypes));
  }


  //====================================================================================================================

  /**
   * Implement this class to validate SearchQueryBeans in every test method.
   */
  private abstract class Validator {

    /**
     * Implement assertions here.
     */
    public abstract void validate(SearchQueryBean searchQueryBean);
  }

  //--------------------------------------------------------------------------------------------------------------------

  /**
   * Determines the index of the nth occurrence of a given character of a string.
   * @param str The string to analyze.
   * @param separator The character to look for.
   * @param n The nth occurrence to look for.
   * @return Index of the nth occurrence of the given character in the given string. -1 if there is no such occurrence.
   */
  private static int nthIndexOf(String str, char separator, int n) {
    int pos = str.indexOf(separator, 0);
    for (int i=1; pos!=-1 && i<n; ++i) {
      pos = str.indexOf(separator, pos+1);
    }
    return pos;
  }

  /**
   * A implementation of a SearchResultFactory that allows validation of the SearchQueryBean build in the various SearchService methods.
   */
  private static class LocalSearchResultFactory implements SearchResultFactory {

    private final Validator validator;
    private final SearchResultBean searchResultBean;

    public LocalSearchResultFactory(Validator validator) {
      this.validator = validator;

      searchResultBean = new SearchResultBean();
      searchResultBean.setFacetResult(new FacetResult(ImmutableMap.of(SearchConstants.FIELDS.TEXTBODY.toString(),
        singletonList(new FacetValue(SearchConstants.FIELDS.TEXTBODY.toString(), TERM_NAME, TERM_COUNT)))));
      searchResultBean.setAutocompleteSuggestions(singletonList(new ValueAndCount(TERM_NAME, TERM_COUNT)));
    }

    @Override
    public SearchResultBean createSearchResult(SearchQueryBean searchInput, long cacheForInSeconds) {
      validator.validate(searchInput);

      return searchResultBean;
    }

    @Override
    public SearchResultBean createSearchResultUncached(SearchQueryBean searchInput) {
      validator.validate(searchInput);

      return searchResultBean;
    }
  }

}
