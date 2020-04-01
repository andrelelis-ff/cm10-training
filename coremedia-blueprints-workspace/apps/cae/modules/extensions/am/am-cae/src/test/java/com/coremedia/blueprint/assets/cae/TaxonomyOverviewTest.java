package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TaxonomyOverviewTest {

  @Test
  public void equalsHashcode_differentAttributes_differentObject() {
    CMTaxonomy taxonomy = mock(CMTaxonomy.class);
    CMTaxonomy differentTaxonomy = mock(CMTaxonomy.class);

    TaxonomyOverview taxonomyOverview = new TaxonomyOverview(taxonomy);
    TaxonomyOverview equalTaxonomyOverview = new TaxonomyOverview(taxonomy);
    TaxonomyOverview differentTaxonomyOverview = new TaxonomyOverview(differentTaxonomy);

    //noinspection EqualsWithItself
    assertTrue("TaxonomyOverview objects should not be different", taxonomyOverview.equals(taxonomyOverview));
    assertTrue("TaxonomyOverview objects should not be different", taxonomyOverview.equals(equalTaxonomyOverview));
    assertFalse("TaxonomyOverview objects should be different", taxonomyOverview.equals(differentTaxonomyOverview));
    //noinspection ObjectEqualsNull
    assertFalse("TaxonomyOverview objects should be different", taxonomyOverview.equals(null));
    assertEquals("Equal TaxonomyOverviews should have equal hashCodes", taxonomyOverview.hashCode(), equalTaxonomyOverview.hashCode());
    assertNotEquals("Different TaxonomyOverviews should have different hashCodes", taxonomyOverview.hashCode(), differentTaxonomyOverview.hashCode());
  }

  @Test
  public void getters_provideLegalValues_valuesShouldNotBeModified() {
    AMTaxonomy taxonomy = mock(AMTaxonomy.class);

    TaxonomyOverview taxonomyOverview = new TaxonomyOverview(taxonomy);

    assertEquals("Taxonomy should stay the same", taxonomy, taxonomyOverview.getTaxonomy());
    assertTrue(taxonomyOverview.toString().contains(TaxonomyOverview.class.getSimpleName()));
  }

  @Test
  public void testEmptySearchTerm() {
    CMTaxonomy taxonomy = mock(CMTaxonomy.class);
    assertEquals("", new TaxonomyOverview(taxonomy).getSearchTerm());
  }

}