package com.coremedia.blueprint.assets.cae;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchOverviewTest {

  @Test
  public void query() {
    String query = "test";
    SearchOverview overview = new SearchOverview(query);

    assertEquals(query, overview.getSearchTerm());
  }
  @Test
  public void equals_hashCode() {
    String query1 = "test1";
    SearchOverview overview = new SearchOverview(query1);
    SearchOverview equalOverview = new SearchOverview(query1);
    String query2 = "test2";
    SearchOverview differentOverview = new SearchOverview(query2);

    //noinspection EqualsWithItself
    assertTrue(overview.equals(overview));
    assertFalse(overview.equals(differentOverview));
    assertTrue(overview.equals(equalOverview));
    //noinspection ObjectEqualsNull
    assertFalse(overview.equals(null));

    assertNotEquals(overview.hashCode(), differentOverview.hashCode());
    assertEquals(0, new SearchOverview(null).hashCode());

    assertNotNull(overview.toString());
  }
}
