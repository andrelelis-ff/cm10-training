package com.coremedia.blueprint.personalization.search;

import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.search.SearchFunctionArguments;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests {@link SolrLastVisitedIDs}
 */
public class SolrLastVisitedIDsTest {
  public static final String DEFAULT_FIELD = "id";
  private static final String CONTEXT_NAME = "last_visited";

  private ContextCollection contextCollection;
  private SolrLastVisitedIDs solrLastVisitedIDs;

  @Before
  public void setUp() {
    contextCollection = new ContextCollectionImpl();
    contextCollection.setContext(CONTEXT_NAME, new PropertyProfile());

    solrLastVisitedIDs = new SolrLastVisitedIDs();

    solrLastVisitedIDs.setDefaultContextName(CONTEXT_NAME);
    solrLastVisitedIDs.setDefaultField(DEFAULT_FIELD);

  }

  @Test
  // tests the Spring injected getter and setter
  public void testSpringGetterAndSetter() {
    Assert.assertEquals(CONTEXT_NAME, solrLastVisitedIDs.getDefaultContextName());
    Assert.assertEquals(DEFAULT_FIELD, solrLastVisitedIDs.getDefaultField());
  }

  @Test
  // tests if args params will overwrite default values
  public void testArgsVersusDefaultParams() {

    // setup valid context
    Integer testId1 = 4712;
    PropertyProfile lastVisitedContext = new PropertyProfile();
    lastVisitedContext.setProperty("pagesVisited", Arrays.asList(testId1));
    contextCollection.setContext(CONTEXT_NAME, lastVisitedContext);

    // test in context
    String myField = "thisIsTheTestField";
    String[] params = new String[]{SolrLastVisitedIDs.SEARCH_ENGINE_FIELD_PARAMETER + ":" + myField};

    SearchFunctionArguments searchFunctionArguments = new SearchFunctionArguments(params);

    Assert.assertEquals("thisIsTheTestField:(\"contentbean:4712\")", solrLastVisitedIDs.evaluate(contextCollection, searchFunctionArguments));
    Assert.assertEquals("id:(\"contentbean:4712\")", solrLastVisitedIDs.evaluate(contextCollection, new SearchFunctionArguments()));
  }

  @Test
  //tests if a search string is not empty and syntactical correct if the context params are empty (e.g. no visited pages)
  public void testEmptyContextParamsReturnNonEmptySolrString() {
    contextCollection.setContext(CONTEXT_NAME, new PropertyProfile());

    String solrString = solrLastVisitedIDs.evaluate(contextCollection, new SearchFunctionArguments());

    // not empty
    Assert.assertTrue(solrString != null);
    Assert.assertTrue(!solrString.isEmpty());
    // should not contain id()
    Assert.assertFalse(solrString.contains("id()"));
  }

  @Test
  //tests if the search string is not empty even if the context is null
  public void testContextIsNullButSolrStringIsNonEmpty() {
    String myContext = "thisIsANonExistingContext";
    String[] params = new String[]{SolrLastVisitedIDs.CONTEXT_NAME_PARAMETER + ":" + myContext};

    SearchFunctionArguments searchFunctionArguments = new SearchFunctionArguments(params);

    Assert.assertNotNull(solrLastVisitedIDs.evaluate(contextCollection, searchFunctionArguments));
  }

  @Test
  //tests if the visited IDs will be set in the Solr string
  public void testSolrStringContainsVisitedIDs() {
    PropertyProfile lastVisitedContext = new PropertyProfile();

    Integer testId1 = 4712;
    Integer testId2 = 4714;

    lastVisitedContext.setProperty("0", testId1.intValue());
    lastVisitedContext.setProperty("1", testId2.intValue());
    lastVisitedContext.setProperty("pagesVisited", Arrays.asList(testId1, testId2, 4716));
    contextCollection.setContext(CONTEXT_NAME, lastVisitedContext);

    String solrString = solrLastVisitedIDs.evaluate(contextCollection, new SearchFunctionArguments());

    Assert.assertTrue(solrString.contains(testId1.toString()));
    Assert.assertTrue(solrString.contains(testId2.toString()));

    Assert.assertEquals("id:(\"contentbean:4712\"^3 OR \"contentbean:4714\"^2 OR \"contentbean:4716\")", solrString);
  }


}
