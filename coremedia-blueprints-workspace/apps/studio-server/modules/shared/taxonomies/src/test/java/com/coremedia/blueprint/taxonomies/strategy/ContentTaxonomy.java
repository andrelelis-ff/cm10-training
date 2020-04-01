package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.cap.content.Content;
import org.mockito.Mockito;

import static org.mockito.Mockito.lenient;

/**
 * Interface especially exists to help Mockito with Generics.
 * In addition to that, provides factory methods for Mocks.
 */
interface ContentTaxonomy extends Taxonomy<Content> {
  static Taxonomy<Content> createTaxonomy(String id,
                                          String siteId,
                                          boolean valid) {
    Taxonomy<Content> taxonomy = Mockito.mock(ContentTaxonomy.class);
    lenient().doReturn(id).when(taxonomy).getTaxonomyId();
    lenient().doReturn(valid).when(taxonomy).isValid();
    lenient().doReturn(siteId).when(taxonomy).getSiteId();
    return taxonomy;
  }

  static Taxonomy<Content> createTaxonomy() {
    return createTaxonomy(true);
  }

  static Taxonomy<Content> createTaxonomy(boolean valid) {
    return createTaxonomy("", valid);
  }

  @SuppressWarnings("SameParameterValue")
  static Taxonomy<Content> createTaxonomy(String id, boolean valid) {
    return createTaxonomy(id, null, valid);
  }
}
