package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Strategy to create a content-based taxonomy.
 * This function exists especially for providing different strategies for
 * creating taxonomies within tests.
 */
@FunctionalInterface
@DefaultAnnotation(NonNull.class)
public interface CreateTaxonomyFunction {
  /**
   * Create a taxonomy from given folder, siteId and taxonomy type.
   *
   * @param rootFolder   root folder.
   * @param siteId       site id; {@code null} if not part of a site.
   * @param taxonomyType type of taxonomies.
   * @return taxonomy.
   */
  Taxonomy<Content> apply(Content rootFolder, @Nullable String siteId, ContentType taxonomyType);
}
