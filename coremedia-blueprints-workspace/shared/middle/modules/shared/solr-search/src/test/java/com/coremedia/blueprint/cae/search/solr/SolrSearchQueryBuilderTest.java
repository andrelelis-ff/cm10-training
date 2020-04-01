package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.Value;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for class SolrQueryBuilder
 */
public class SolrSearchQueryBuilderTest {

  private static SolrSearchQueryBuilder createBuilder() {
    return new SolrSearchQueryBuilder();
  }

  @Test
  public void testDefaults() {
    SolrSearchQueryBuilder qb = createBuilder();
    SearchQueryBean query = new SearchQueryBean();
    SolrQuery sq = qb.buildQuery(query);
    assertTrue(sq.getFilterQueries().length == 1);
    assertEquals("feederstate:SUCCESS AND notsearchable:false", sq.getFilterQueries()[0]);
    assertTrue(sq.getFacetFields() == null);
  }

  @Test
  public void testFilterConversion() {
    Condition filterSingle = Condition.is("bla", Value.exactly("1"));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(filterSingle), ("bla:1"));
    Condition filterAny = Condition.is("bla", Value.anyOf(Arrays.asList("\"contentbean:1\"", "\"contentbean:2\"", "\"contentbean:3\"")));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(filterAny), ("bla:(\"contentbean:1\" OR \"contentbean:2\" OR \"contentbean:3\")"));
    Condition filterAll = Condition.is("bla", Value.allOf(Arrays.asList("1", "2", "3")));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(filterAll), ("bla:(1 AND 2 AND 3)"));
    Condition negatingFilter = Condition.isNot("bla", Value.exactly("1"));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(negatingFilter), ("-bla:1"));
    Condition rangeLower = Condition.lowerThan("bla", Value.exactly("12345"));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(rangeLower), ("bla:[* TO 12345]"));
    Condition rangeGreater = Condition.greaterThan("bla", Value.exactly("12345"));
    Assert.assertEquals(SolrSearchQueryBuilder.convertCondition(rangeGreater), ("bla:[12345 TO *]"));
  }

  @Test
  public void testSetOffset() {
    SolrQuery q = new SolrQuery();
    SolrSearchQueryBuilder.setOffset(q, 4711);
    //noinspection RedundantCast
    assertEquals(4711, (int) q.getStart()); // cast is needed

    SolrSearchQueryBuilder.setOffset(q, 0);
    //noinspection RedundantCast
    assertEquals(0, (int) q.getStart()); // cast is needed

    SolrSearchQueryBuilder.setOffset(q, -1);
    assertNull(q.getStart());
  }

  @Test
  public void testSetLimit() {
    SolrQuery q = new SolrQuery();

    SolrSearchQueryBuilder.setLimit(q, 4711);
    //noinspection RedundantCast
    assertEquals(SolrSearchParams.MAX_LIMIT, (int) q.getRows()); // cast is needed

    SolrSearchQueryBuilder.setLimit(q, 471);
    //noinspection RedundantCast
    assertEquals(471, (int) q.getRows()); // cast is needed

    // This differs from Content Server query builder
    SolrSearchQueryBuilder.setLimit(q, -1);
    //noinspection RedundantCast
    assertEquals(SolrSearchParams.MAX_LIMIT, (int) q.getRows()); // cast is needed
  }

  @Test
  public void testGetQueryClause() {
    assertEquals("", SolrSearchQueryBuilder.getQueryClause(""));
    assertEquals("", SolrSearchQueryBuilder.getQueryClause("*"));
    assertEquals("", SolrSearchQueryBuilder.getQueryClause("?"));
    assertEquals("", SolrSearchQueryBuilder.getQueryClause("+"));
    assertEquals("", SolrSearchQueryBuilder.getQueryClause("-"));

    assertEquals("hello", SolrSearchQueryBuilder.getQueryClause("hello"));
  }

  @Test
  public void testSetQuery() {
    SolrQuery q = new SolrQuery();
    SolrSearchQueryBuilder.setQuery(q, "hello");
    assertEquals("hello", q.getQuery());
  }

  @Test
  public void testCreateQuery() {
    SearchQueryBean input = new SearchQueryBean();
    input.setQuery("hello");
    input.setLimit(471);
    input.setOffset(10);
    SolrSearchQueryBuilder solrQueryBuilder = createBuilder();
    SolrQuery q = solrQueryBuilder.buildQuery(input);
    //assertNotNull(q.getParams(CommonParams.QT));
    //assertEquals(1, q.getParams(CommonParams.QT).length);
    //assertEquals("/cmdismax", q.getParams(CommonParams.QT)[0]);
    assertEquals("id", q.getFields());
    //noinspection RedundantCast
    assertEquals(10, (int) q.getStart()); // cast is needed
    //noinspection RedundantCast
    assertEquals(471, (int) q.getRows()); // cast is needed
    assertEquals("hello", q.getQuery());
  }

  @Test
  public void testLocalParamsEscaping() throws Exception {
    String query = "{!lucene}hello world";
    String expected = "\\{!lucene}hello world";

    SolrSearchQueryBuilder solrQueryBuilder = createBuilder();
    SearchQueryBean input = new SearchQueryBean();
    input.setQuery(query);

    SolrQuery solrQuery = solrQueryBuilder.buildQuery(input);
    assertEquals(expected, solrQuery.getQuery());
  }

  @Test
  public void testSort() {
    SolrSearchQueryBuilder solrQueryBuilder = createBuilder();
    SearchQueryBean input = new SearchQueryBean();
    input.setQuery("hello");
    input.setSortFields(Arrays.asList("a", "b asc", "c dEsc", "termfreq(folderpath, \" + folderId + \") desc",
            "legacy1 ascending", "legacy2 descending"));

    SolrQuery solrQuery = solrQueryBuilder.buildQuery(input);
    assertEquals("hello", solrQuery.getQuery());
    assertEquals(Arrays.asList(
            new SolrQuery.SortClause("a", SolrQuery.ORDER.desc),
            new SolrQuery.SortClause("b", SolrQuery.ORDER.asc),
            new SolrQuery.SortClause("c", SolrQuery.ORDER.desc),
            new SolrQuery.SortClause("termfreq(folderpath, \" + folderId + \")", SolrQuery.ORDER.desc),
            new SolrQuery.SortClause("legacy1", SolrQuery.ORDER.asc),
            new SolrQuery.SortClause("legacy2", SolrQuery.ORDER.desc)), solrQuery.getSorts());
  }
}
