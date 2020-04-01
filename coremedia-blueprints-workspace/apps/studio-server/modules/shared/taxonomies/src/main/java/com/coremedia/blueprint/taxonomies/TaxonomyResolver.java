package com.coremedia.blueprint.taxonomies;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;

/**
 * Resolve the ITaxonomy that represent a taxonomy tree.
 */
public interface TaxonomyResolver {

  /**
   * Returns the taxonomy that matches the given taxonomy id.
   *
   * @return the taxonomy that matches the given taxonomy id; {@code null} if not found.
   */
  @Nullable
  Taxonomy getTaxonomy(String siteId, String taxonomyId);

  /**
   * Returns the collection of detected taxonomies.
   *
   * @return the collection of detected taxonomies.
   */
  Collection<Taxonomy> getTaxonomies();


  /**
   * Method for manual reload of taxonomies, e.g. after server imports.
   *
   * @deprecated Explicit reload is not robust. Use automatic invalidation for taxonomies instead, like using CacheKeys.
   */
  @Deprecated(since = "1910")
  default boolean reload() {
    return true;
  }
}
